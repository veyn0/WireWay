package xyz.wireway.protocol.packet;

import xyz.wireway.protocol.Packet;
import xyz.wireway.protocol.PacketId;

import java.nio.ByteBuffer;

@PacketId("xyz.wireway.system.monitor.heartbeat")
public class HeartBeatPacket implements Packet {

    private static int idCount = 0;

    private long timestamp;
    private int id;

    public HeartBeatPacket(){
        timestamp = System.currentTimeMillis();
        id = idCount;
        idCount++;
    }

    @Override
    public ByteBuffer encode() {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES + Integer.BYTES);
        buffer.putLong(timestamp);
        buffer.putInt(id);
        buffer.flip();
        return buffer;
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
