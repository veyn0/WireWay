# Restructuring the transmission layer

A plan for `frame.transmit` and the layers directly around it. The goal is
maintainability and legibility, not new features: the same bytes should go over
the wire when it is done.

---

## 1. What the current layout costs

### 1.1 `FramedDataOutput` holds five responsibilities

In 110 lines it is the stream-id allocator, the round-robin scheduler, the frame
assembler, the transport writer, and the owner of a thread — plus argument
validation. None of the five can be exercised without the other four, so the
budget arithmetic in `buildNextFrame` (a nested loop over a mutable deque with
three interacting counters) can only be checked by reading it.

### 1.2 The wire format is not written down, so it is re-implemented per layer

Four layers each carry a varint length prefix — frame, fragment, stream header,
packet record — and four different pieces of code independently answer *"do I
have a whole record yet?"*:

| Location | Mechanism |
| --- | --- |
| `ProtocolUtils.canRead(ComposedBuffer)` | `peek(max(remaining, 5))`, catches `BufferOverflowException` |
| `ProtocolUtils.canRead(ByteBuffer)` | `readVarIntSafe`, catches `BufferOverflowException` |
| `SequentialPacketChannel.canRead` | `peek(min(11, remaining))`, catches everything, never checks the length |
| `IncomingChannelWrapper.write` | `readVarIntSafe`, rethrows underflow as `RuntimeException` |

All four are wrong, each in a different way (C1–C4 in `BUGS.md`). That is not
four bugs; it is one missing abstraction, expressed four times. `spec/` is empty,
so there is no reference to check them against either.

### 1.3 `Channel` is a union of two roles

```java
int availableBytes();  void read(ByteBuffer, int);  boolean isExhausted();   // outbound only
void write(ByteBuffer);  void close();                                       // inbound only
void inject(C);  void setSubId(int);  int getSubId();                        // lifecycle
```

Every implementation must supply all eight regardless of direction. The
consequences are visible: `SequentialPacketChannel.isExhausted()` returns a
constant `false` that really means "outbound, never ends";
`AsyncPacketChannel` re-overrides `isExhausted()` with the base implementation and
declares a `subId` field that shadows an inherited one and is never read (M9).

### 1.4 Behaviour arrives through an abstract base with hooks

`ComposedBufferBase` supplies state through `protected` fields and calls
`postWrite()` from *inside* `add()`. So decoding, dispatch, the application
listener, and the responder's write-back all execute on the transport read thread,
nested inside a buffer mutation. That is the direct cause of H2: the responder in
`PacketStreamController.onPacketReceive` writes into an **outbound** channel
buffer from the **inbound** thread.

### 1.5 The threading model is implicit

One writer thread is created inside `FramedDataOutput`'s constructor, one reader
thread inside `SocketTransport`'s. Nothing states which thread runs user callbacks,
which objects are confined to which thread, or how either thread stops (M1). The
races in H1–H3 are what fills that vacuum.

### 1.6 Four different ids, three of them called "channel"

`dataId` (fragment header) · `channelId` (`OutgoingChannelWrapper`) ·
`channelTypeId` (registry) · `subId` (application address). The first two are the
same thing under two names; the third and fourth are unrelated to both.

### 1.7 Inbound and outbound do the same job in mirror-image styles

Prepending vs. parsing the stream header is one concept implemented twice:
`OutgoingChannelWrapper` splices a `ByteBuffer` with manual limit juggling;
`IncomingChannelWrapper` accumulates into a `ComposedBuffer` and uses exceptions
for control flow. Neither is obviously the inverse of the other, which is exactly
what makes C4 easy to miss.

---

## 2. Target shape

### 2.1 Vocabulary first

| Now | Proposed | Meaning |
| --- | --- | --- |
| `dataId`, `channelId` | `streamId` | Transient per-connection id in the fragment header. Allocated by the sender, released after the end fragment. |
| `channelTypeId` | `channelTypeId` | Negotiated numeric id of the channel *class*. Unchanged. |
| `subId` | `endpointId` | Application-level address inside one channel type. |

This is a pure rename and costs nothing, but it is what makes the rest readable.

### 2.2 Packages

```
wire/                     pure codec — no threads, no state beyond cursors
  VarInt
  LengthPrefixed          the ONE place that knows "varint length + payload"
  FragmentHeader          record(streamId, flags, payloadLength)
  Frame, FrameFragment
  StreamHeader            record(channelTypeId, endpointId)   <- today: untyped metadata bytes

channel/
  DataSource              int available();  void read(ByteBuffer, int);  boolean isComplete();
  DataSink                void write(ByteBuffer);  void close();
  ChannelType<C>          construction + context injection
  BufferedSource / BufferedSink     small final helpers, used by composition

transmit/
  inbound/
    FrameDecoder          bytes  -> Frame          (owns the receive buffer)
    InboundDemux          Frame  -> InboundStream  (owns the streamId map and lifecycle)
    InboundStream         fragments -> StreamHeader + DataSink
  outbound/
    OutboundStream        DataSource + streamId + StreamHeader
    StreamRegistry        streamId allocation + the ready set
    FrameAssembler        chunks under a byte budget -> Frame     (pure, no threads)
    TransmitLoop          the only class that owns a thread
```

Dependencies point one way: `wire` knows nothing; `channel` depends on `wire`;
`transmit` depends on both; `service` depends on `transmit`. Nothing in the
library depends on `Main` (L2).

---

## 3. The moves, in order of value

### Move 1 — Extract `LengthPrefixed`

```java
public final class LengthPrefixed {
    public static boolean hasRecord(ComposedBuffer b);   // peek min(remaining, 5)
    public static ByteBuffer takeRecord(ComposedBuffer b);
    public static void writeRecord(ByteBuffer out, ByteBuffer payload);
}
```

`ProtocolUtils.canRead` (both overloads), `Frame.read(ComposedBuffer)`,
`Packet.read(ComposedBuffer)` and `SequentialPacketChannel.canRead` all collapse
into calls to it. "Not enough bytes yet" becomes a return value instead of an
exception, which is what removes C1, C2, C3 and makes C4 expressible at all.

This is the highest-value single change in the plan and the one class most worth
testing byte by byte.

### Move 2 — Split `Channel` into `DataSource` and `DataSink`

Outbound code sees only `DataSource`, inbound only `DataSink`. A bidirectional
channel implements both, but the two directions no longer share one
`ComposedBuffer` by accident. `ComposedBufferBase` disappears in favour of two
small final helpers held by composition, so a subclass cannot shadow a field it
does not own (M9) and cannot inherit an empty `postClose()` it has to implement
anyway.

### Move 3 — Make the stream header a value type

```java
record StreamHeader(int channelTypeId, int endpointId) {
    void encode(ByteBuffer out);
    static Optional<StreamHeader> decode(ComposedBuffer in);   // empty == need more bytes
}
```

`OutgoingChannelWrapper`'s limit juggling and `IncomingChannelWrapper`'s try/catch
both become one call. The two wrappers shrink to roughly twenty lines each and
finally read as mirror images.

### Move 4 — Separate scheduling from assembly from writing

```java
interface StreamScheduler {
    OutboundStream next();
    void onReady(OutboundStream s);
    void onDrained(OutboundStream s);
}
final class RoundRobinScheduler implements StreamScheduler { /* today's deque logic, alone */ }

final class FrameAssembler {
    Frame assemble(StreamScheduler s, int maxFrameBytes, int maxFragmentBytes);   // pure
}

final class TransmitLoop { /* owns the thread; assembler -> Transport */ }
```

`FrameAssembler` becomes a function of (scheduler state, budgets) → `Frame`, so
the budget arithmetic — including the missing length-prefix charge in M6 — can be
asserted in a test instead of inferred from a nested loop. `RoundRobinScheduler`
becomes the single place where fairness is defined, and the natural home for a
future priority or credit-based policy.

### Move 5 — Make the transmit loop event-driven and stoppable

Replace `while (true) { sendFrame(); sleep(100); }` with one writer thread parked
on the ready set (a `ReentrantLock` + `Condition`, or a
`LinkedBlockingQueue<OutboundStream>`). `addChannel` and `addPacket` signal
readiness. The loop exits on `ConnectionState.CLOSED` or an explicit `stop()`. The
thread is a daemon, created by an injected `ThreadFactory`/`Executor` rather than
by a constructor. Fixes M1 and removes the 100 ms floor under every message.

### Move 6 — State the threading contract, then enforce it

One inbound thread (the transport's), one outbound thread (`TransmitLoop`).
Decoding may run inline on the inbound thread; **application callbacks must not**.
Decoded packets go to a `Dispatcher` — a serial `Executor`, single-threaded by
default — so `PacketStreamController.onPacketReceive` and user listeners never run
nested inside a buffer mutation and never touch outbound buffers from the read
thread. That is what actually removes H2, rather than wrapping a lock around the
symptom. Write the contract into `transmit/package-info.java`.

While here: give `Transport`/`TransportListener` an `onError(Throwable)` /
`onClose()` path so a decode failure closes the connection with a reason instead
of silently killing a thread (C5), and attach listeners **before** starting the
read loop (H3).

### Move 7 — Give streams an explicit lifecycle

`InboundStream` and `OutboundStream` get `OPEN / HALF_CLOSED / CLOSED`. A
`streamId` is released only after the end fragment has been *written* (outbound)
or *seen* (inbound), and the inbound map entry is removed on close. This fixes
H4 and H7 and closes the id-reuse window that currently exists between
`channelIdAllocator.release()` and the receiver observing the end flag.

---

## 4. Sequencing

Each step compiles on its own and is reviewable in isolation.

| # | Step | Removes |
| --- | --- | --- |
| 1 | Vocabulary rename; write the wire format into `spec/` and `package-info.java`. No behaviour change. | L5 |
| 2 | `LengthPrefixed` + `StreamHeader`; rewrite the four ad-hoc parsers on top. | C1–C4, M5 |
| 3 | Split `Channel` into `DataSource`/`DataSink`; delete `ComposedBufferBase`. | M9, M10 |
| 4 | Split `FramedDataOutput` into `RoundRobinScheduler` / `FrameAssembler` / `TransmitLoop`. | M6, M7 |
| 5 | Event-driven stoppable loop; threading contract; `Dispatcher`; transport error path. | C5, H1, H2, H3, M1 |
| 6 | Stream lifecycle, id release, inbound map cleanup, response timeouts. | H4, H6, H7 |
| 7 | Bounds and validation: max frame length, max buffered bytes, registry error messages. | M3, M4, M8 |

Steps 1–2 are worth doing before anything else — they are cheap and they retire
every critical defect.

## 5. Test seams the split creates

There are none today (L6). After the split the natural units are:

- `LengthPrefixed` — byte-by-byte, including every split position of a record.
- `FrameAssembler` — budget and fairness, no threads, no transport.
- `InboundDemux` — fragment ordering, split stream headers, unknown `streamId`,
  id reuse after close.
- A loopback `Transport` pair for end-to-end tests: feed a payload much larger
  than `maxFragmentLen` and assert it arrives intact. This is the scenario that
  currently fails (C1), so it belongs in the suite from day one.
