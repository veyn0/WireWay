# Known defects

Findings from a read of `0.1-ALPHA` (commit `e13fa6a`). Items marked **verified**
were reproduced against the compiled tree; the rest are from inspection.

> **Status:** all findings below were addressed by the transmission-layer
> refactoring described in `REFACTOR-PLAN.md`. File/line references point at
> the old layout as of commit `e13fa6a`.

---

## Critical — breaks normal operation

### C1 — A packet larger than one fragment kills the receiving connection
`frame/channel/impl/SequentialPacketChannel.java:46-58`, crash at `:40`

`canRead()` parses the record header and then `return true` without ever comparing
the declared packet length against the bytes actually buffered. `postWrite()` then
calls `Packet.read()`, which tries to consume the whole record.

**Verified** — two `WireWay` peers with `maxFragmentLen = 32`, one 200-byte packet:

```
java.lang.IllegalArgumentException: Not enough bytes: requested 203, have 32
    at xyz.wireway.util.ComposedBuffer.checkAvailable(ComposedBuffer.java:57)
    at xyz.wireway.protocol.Packet.read(Packet.java:28)
    at xyz.wireway.frame.channel.impl.SequentialPacketChannel.postWrite(SequentialPacketChannel.java:40)
    at xyz.wireway.frame.transmit.FramedDataInput.onReceive(FramedDataInput.java:29)
```

Any payload that does not fit into a single fragment is affected, which is the
normal case. The same method also has the inverse problem: `if (len < 4) return false`
rejects a complete record whose header is 3 bytes, so it stalls forever.

### C2 — `ProtocolUtils.canRead(ComposedBuffer)` throws on any short read
`util/ProtocolUtils.java:46`

```java
int peekLength = Math.max(totalLength, 5);   // must be Math.min
```

With fewer than 5 buffered bytes this peeks past the end and
`ComposedBuffer.checkAvailable` throws. Every TCP read that leaves a partial frame
header in the buffer hits this.

**Verified** — `FramedDataInput.onReceive(new byte[]{0x20, 0x00})` →
`IllegalArgumentException: Not enough bytes: requested 5, have 2`.

### C3 — Both `canRead` overloads catch the wrong exception
`util/ProtocolUtils.java:37` and `:50`

The catch clause is `BufferOverflowException`, but `VarInt.readVarInt` /
`readVarIntSafe` signal exhaustion with `BufferUnderflowException` and an
over-long varint with `IllegalStateException`. Neither is caught, so both escape
as errors where a `false` return was intended.

**Verified** — `canRead(ByteBuffer.wrap(new byte[]{(byte)0x80}))` throws
`BufferUnderflowException`; a 6-byte continuation run throws `IllegalStateException`.

### C4 — A stream header split across two fragments kills the connection
`frame/transmit/IncomingChannelWrapper.java:44-46`

The class buffers metadata precisely so a split header can be handled, then throws
it away:

```java
} catch (BufferUnderflowException e) {
    throw new RuntimeException(e);   // should return and wait for more bytes
}
```

**Verified** — writing the first byte of a 2-byte header throws
`RuntimeException: java.nio.BufferUnderflowException`.

### C5 — Receive-path exceptions silently kill the reader thread
`transport/adapter/SocketTransport.java:50-68`

The read loop catches `IOException` only. Any `RuntimeException` raised by a
listener — that is, C1 through C4 — terminates the thread. The `finally` block
calls `close()`, which only flips an enum (see H5), so the socket stays open and
the connection looks alive while receiving nothing. There is no error callback on
`Transport`/`TransportListener`, so the failure is invisible to the application.

---

## High — data races and unguarded state

### H1 — `FramedDataOutput.dataSources` is an unsynchronized `ArrayDeque`
`frame/transmit/FramedDataOutput.java:15`, `:42-46`, `:74-105`

`addChannel()` runs on caller threads; `buildNextFrame()` runs on the sender
thread. `ArrayDeque` gives no visibility or atomicity guarantees — a concurrent
`add` during the `pollFirst()` pass can yield a corrupted deque or a `null` from
`pollFirst()`, which then NPEs on `.availableBytes()`.

### H2 — Outbound channel buffers are written and drained from different threads
`frame/channel/impl/SequentialPacketChannel.java:22-28`, `service/packetstream/PacketStreamController.java:47`

`addPacket()` appends to the channel's `ComposedBuffer` from the caller thread and
— when a listener answers a request — from the *receive* thread, while the sender
thread drains the same buffer through `OutgoingChannelWrapper.read()`.
`ComposedBuffer` is not thread-safe and reallocates/compacts its backing array in
`ensureWritable`, so this can hand out views over data that has already moved.

### H3 — Listeners are registered after the reader thread has started
`transport/adapter/SocketTransport.java:27` vs. `service/WireWay.java:55`

`SocketTransport`'s constructor starts the read loop; the `FramedDataInput`
listener is only attached later by the `WireWay` constructor. Bytes arriving in
that window are dropped without a trace. `transportListeners` is also a plain
`ArrayList` iterated on the read thread while `addListener` mutates it →
`ConcurrentModificationException`.

### H4 — `FramedDataInput` never checks that a fragment belongs to a known stream
`frame/transmit/FramedDataInput.java:42-50`

```java
if (f.isStart()) channelsById.put(dataId, new IncomingChannelWrapper(...));
IncomingChannelWrapper channel = channelsById.get(dataId);
channel.write(f.getData());          // NPE when no start fragment was seen
if (f.isEnd()) channel.close();      // entry is never removed
```

A fragment for an unknown `dataId` (reordering, a peer bug, a malformed frame)
NPEs on the receive thread. Closed wrappers stay in the map forever.

### H5 — `SocketTransport.close()` does not close the socket
`transport/adapter/SocketTransport.java:88-90`

It only assigns `ConnectionState.CLOSED`. The socket, its streams and the reader
thread all leak. Nothing reads `getConnectionState()` before sending either —
`FramedDataOutput.java:70` still carries the `TODO` — so the sender keeps writing
to a dead connection until the `IOException` arrives.

### H6 — Three unguarded map lookups on the receive path
`service/packetstream/PacketStreamController.java:43-52`

`listenerBySubId.get(...)`, `channelsBySubId.get(...)` and
`responseConsumerByReferenceIdBySubId.get(subId)` are all dereferenced without a
null check. A packet for a sub-id that has no listener, no channel, or has never
sent a request NPEs on the receive thread → C5. A fire-and-forget request
(`referenceId == 0`) whose listener returns a packet also lands here.

### H7 — Response consumers and reference ids are never released
`service/packetstream/PacketStreamController.java:38`, `:73-76`

`packetReferenceIdAllocator.allocate()` is never paired with `release()`, and the
consumer is not removed from `responseConsumerByReferenceIdBySubId` after it fires.
Both maps grow for the lifetime of the connection, and a caller that never gets an
answer pins its callback forever. There is no timeout path.

---

## Medium

### M1 — The sender thread cannot be stopped and adds 100 ms to every send
`frame/transmit/FramedDataOutput.java:48-59`

`while (true) { sendFrame(); Thread.sleep(100); }` on a non-daemon thread started
from the constructor. **Verified** — the `Main` demo completes its work and then
hangs; the JVM only exits on `SIGTERM`. There is no stop condition, no reaction to
`ConnectionState.CLOSED`, and every message waits up to a full tick.

### M2 — `WireWay` mutates the caller's `ChannelSet`
`service/WireWay.java:50-51`

The constructor registers the two built-in channel types into the set it was
handed. Passing the same `ChannelSet` to a second `WireWay` throws
`IllegalArgumentException: Channel name hash collision`.

### M3 — Registry lookups NPE on unboxing
`frame/channel/ChannelRegistry.java:32`, `protocol/PacketRegistry.java:29`

`return channelIdsByClass.get(channel.getClass());` unboxes `null` for an
unregistered type. `createChannel`/`createPacket` throw a clear
`IllegalArgumentException` — the reverse direction should too.

### M4 — `Protocol.register` accepts collisions and duplicates silently
`protocol/Protocol.java:12-17`

Unlike `ChannelSet.register`, it overwrites on a hash collision. Because ids are
assigned by sorting the hash set, a silently dropped entry shifts every id after
it and desynchronises the two peers' numbering.

### M5 — `Frame.read` does not validate the fragment sum
`frame/Frame.java:42-46`

The loop runs `while (totalLength < length)` without checking that the fragments
end exactly on the frame boundary. A corrupt fragment length reads past the frame
into the following bytes.

### M6 — Frame budget ignores the frame's own length prefix
`frame/transmit/FramedDataOutput.java:76-104`

`remainingSize` is charged only for fragment lengths, but `Frame.write` also emits
a varint length header. An assembled frame can exceed `maxFrameLength` by up to
5 bytes.

### M7 — Misleading validation and reversed parameter order
`frame/transmit/FramedDataOutput.java:30-31`, `service/WireWay.java:44`, `:57`

`if (maxFrameFragmentLength < 16) throw ... "must be at least 8"` and
`if (maxFrameLength < 32) throw ... "must be at least 12"` — the messages state
different bounds than the checks enforce. `WireWay(..., maxFrameLen, maxFragmentLen)`
also takes its two size arguments in the opposite order to
`FramedDataOutput(..., maxFrameFragmentLength, maxFrameLength)`.

### M8 — No upper bound on buffered bytes
`util/ComposedBuffer.java:61-76`, `util/ProtocolUtils.java:42-53`

Nothing caps the declared frame length or `ComposedBuffer` growth. A peer that
announces a large length makes the receiver buffer until `OutOfMemoryError`.

### M9 — `AsyncPacketChannel` shadows an inherited field
`frame/channel/impl/AsyncPacketChannel.java:16`, `:44-47`

`private int subId` shadows `ComposedBufferBase.subId`. The inherited
`setSubId`/`getSubId` resolve against the base field, so the declared one is dead
and reads as if it were in use. `isExhausted()` is also re-overridden with the
identical base implementation.

### M10 — `ComposedBufferBase.open` is written but never read
`frame/channel/ComposedBufferBase.java:11`, `:33-36`

`close()` sets it; nothing checks it. Writes to a closed channel are accepted.

### M11 — Fragments alias a buffer that can be compacted underneath them
`frame/FrameFragment.java:37`, `util/ComposedBuffer.java:61-76`

`FrameFragment.read` slices its parent buffer, which on the receive path is a view
over `ComposedBuffer`'s internal array. `ensureWritable` compacts that array
**in place** with `System.arraycopy`. The current call order consumes fragments
before the next `add()`, so it holds today — but nothing enforces it, and any
reordering corrupts already-handed-out fragments silently.

---

## Low

- **L1** `protocol/packet/HeartBeatPacket.java:11`, `:18-19` — `static int idCount`
  incremented without synchronisation; duplicate ids across threads.
- **L2** `frame/Frame.java:3`, `frame/FrameFragment.java:3`,
  `util/ProtocolUtils.java:4`, `transport/adapter/SocketTransport.java:3` — library
  classes import `Main`. Leftover debug wiring; the library depends on the demo
  entry point.
- **L3** `transport/adapter/SocketTransport.java:52-57` — one 4096-byte array is
  reused across reads and handed to listeners as a wrapping view. Correct only
  because every listener copies synchronously; an async listener sees corruption.
- **L4** `transport/adapter/SocketTransport.java:92-101` — `listen()` invokes
  `onConnect` on the accept thread, so a slow handler blocks new connections, and
  the `ServerSocket` is only released when the loop throws.
- **L5** `spec/frame/frame.md`, `spec/frame/fragment.md`, `spec/packet/packet.md`
  are empty. There is no written description of the wire format anywhere.
- **L6** No tests exist.
