package xyz.wireway.protocol.packet;

import xyz.wireway.protocol.Packet;
import xyz.wireway.protocol.PacketId;

import java.nio.ByteBuffer;

@PacketId(packetId = "xyz.wireway.packet.system.handshake")
public class HandshakePacket implements Packet {

    @Override
    public Packet decode(ByteBuffer buffer) {
        return null;
    }

    @Override
    public ByteBuffer encode() {
        return null;
    }
}
