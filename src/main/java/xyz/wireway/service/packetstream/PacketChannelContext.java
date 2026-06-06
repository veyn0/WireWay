package xyz.wireway.service.packetstream;

import xyz.wireway.protocol.Packet;
import xyz.wireway.protocol.PacketRegistry;
import xyz.wireway.service.PacketInfo;

import java.util.function.BiConsumer;
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
