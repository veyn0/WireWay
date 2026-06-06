package xyz.wireway.protocol.packet;

import xyz.wireway.protocol.Packet;
import xyz.wireway.protocol.PacketId;

import java.nio.ByteBuffer;

@PacketId(packetId = "xyz.wireway.system.monitor.heartbeat")
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
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES + Integer.BYTES); // 12 Bytes
        buffer.putLong(timestamp);
        buffer.putInt(id);
        buffer.flip(); // position → 0, limit → 12 (bereit zum Lesen)
        return buffer;
    }

    @Override
    public Packet decode(ByteBuffer buffer) {
        HeartBeatPacket packet = new HeartBeatPacket();
        packet.timestamp = buffer.getLong();
        packet.id = buffer.getInt();
        return packet;
    }

    public int getId() {
        return id;
    }

    public long getTimestamp() {
        return timestamp;
    }

}
