package xyz.wireway.protocol.packet;

import xyz.wireway.protocol.Packet;
import xyz.wireway.protocol.PacketId;

@PacketId(packetId = "xyz.wireway.packet.system.handshake")
public class HandshakePacket implements Packet {
    @Override
    public Packet decode(byte[] data) {
        return null;
    }

    @Override
    public byte[] encode(Packet p) {
        return new byte[0];
    }
}
