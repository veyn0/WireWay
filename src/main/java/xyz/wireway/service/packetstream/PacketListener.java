package xyz.wireway.service.packetstream;

import xyz.wireway.protocol.Packet;

public interface PacketListener {

    Packet onPacketReceive(Packet p);

}
