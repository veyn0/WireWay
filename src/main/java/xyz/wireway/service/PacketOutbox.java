package xyz.wireway.service;

import xyz.wireway.channel.DataSource;
import xyz.wireway.util.ComposedBuffer;
import xyz.wireway.wire.VarInt;

import java.nio.ByteBuffer;

/**
 * Outbound side of a packet stream. Producers append records from any thread;
 * the transmit loop drains. Never completes: the stream stays open.
 */
final class PacketOutbox implements DataSource {

    private final ComposedBuffer buffer = new ComposedBuffer();
    private final Runnable onData;

    PacketOutbox(Runnable onData) {
        this.onData = onData;
    }

    void add(boolean request, int referenceId, ByteBuffer packetRecord) {
        synchronized (this) {
            ByteBuffer meta = ByteBuffer.allocate(1 + VarInt.sizeOf(referenceId));
            meta.put((byte) (request ? 0x01 : 0x00));
            VarInt.writeVarInt(meta, referenceId);
            buffer.add(meta.flip());
            buffer.add(packetRecord);
        }
        // outside the lock: the transmit loop acquires its own lock before touching sources
        onData.run();
    }

    @Override
    public synchronized int available() {
        return buffer.remaining();
    }

    @Override
    public synchronized void read(ByteBuffer dst, int length) {
        dst.put(buffer.get(length));
    }

    @Override
    public boolean isComplete() {
        return false;
    }
}
