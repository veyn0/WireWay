package xyz.wireway.service.packetstream;

import xyz.wireway.protocol.PacketRegistry;

import java.util.function.Consumer;

public class PacketChannelContext {

    private PacketRegistry packetRegistry;
    private Consumer<PacketInfo> onPacketReceivedAtSubChannel;

    public PacketChannelContext(PacketRegistry packetRegistry, Consumer<PacketInfo> onPacketReceivedAtSubChannel) {
        this.packetRegistry = packetRegistry;
        this.onPacketReceivedAtSubChannel = onPacketReceivedAtSubChannel;
    }

    public PacketRegistry getPacketRegistry() {
        return packetRegistry;
    }

    public Consumer<PacketInfo> getOnPacketReceivedAtSubChannel() {
        return onPacketReceivedAtSubChannel;
    }

}
