package xyz.wireway.protocol.packet;

import xyz.wireway.protocol.Packet;
import xyz.wireway.protocol.PacketId;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

@PacketId("xyz.wireway.system.monitor.heartbeat")
public class HeartBeatPacket implements Packet {

    private static final AtomicInteger ID_COUNT = new AtomicInteger();

    private long timestamp;
    private int id;

    public HeartBeatPacket() {
        timestamp = System.currentTimeMillis();
        id = ID_COUNT.getAndIncrement();
    }

    @Override
    public ByteBuffer encode() {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES + Integer.BYTES);
        buffer.putLong(timestamp);
        buffer.putInt(id);
        return buffer.flip();
    }

    @Override
    public void decode(ByteBuffer buffer) {
        this.timestamp = buffer.getLong();
        this.id = buffer.getInt();
    }

    public int getId() {
        return id;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
