package xyz.wireway.service.asyncpacketstream;

import xyz.wireway.protocol.Packet;
import xyz.wireway.protocol.PacketRegistry;

import java.util.function.Consumer;

public class AsyncPacketChannelContext {

    private final PacketRegistry packetRegistry;
    private final Consumer<Packet> onPacketReceive;

    public AsyncPacketChannelContext(PacketRegistry packetRegistry, Consumer<Packet> onPacketReceive) {
        this.packetRegistry = packetRegistry;
        this.onPacketReceive = onPacketReceive;
    }

    public PacketRegistry getPacketRegistry() {
        return packetRegistry;
    }

    public Consumer<Packet> getOnPacketReceive() {
        return onPacketReceive;
    }
}
