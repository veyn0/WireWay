package xyz.wireway.service.stream;

import xyz.wireway.protocol.Packet;

public interface PacketListener {

    void onPacketReceive(Packet p);

}
