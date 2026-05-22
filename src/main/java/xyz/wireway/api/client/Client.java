package xyz.wireway.api.client;

import xyz.wireway.api.WireWayApplication;
import xyz.wireway.api.event.EventListener;
import xyz.wireway.api.packet.PacketRegistry;

public class Client implements WireWayApplication {

    @Override
    public PacketRegistry getPacketRegistry() {
        return null;
    }

    @Override
    public void registerEventListener(EventListener eventListener) {

    }
}
