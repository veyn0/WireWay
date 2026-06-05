package xyz.wireway.frame.datasource;

import xyz.wireway.protocol.Packet;
import xyz.wireway.protocol.PacketRegistry;

public interface Provider {

    void onPacketReceive(Packet p);

    PacketRegistry getPacketRegistry();

}
