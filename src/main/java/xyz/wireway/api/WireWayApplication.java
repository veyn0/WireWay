package xyz.wireway.api;

import xyz.wireway.api.event.EventListener;
import xyz.wireway.api.packet.PacketRegistry;

public interface WireWayApplication {

    public PacketRegistry getPacketRegistry();

    public void registerEventListener(EventListener eventListener);

}
