package xyz.wireway.api.server;

import xyz.wireway.api.WireWayApplication;
import xyz.wireway.api.packet.PacketRegistry;

public class Server implements WireWayApplication {
    @Override
    public PacketRegistry getPacketRegistry() {
        return null;
    }
}
