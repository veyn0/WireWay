package xyz.wireway.service.stream;

import xyz.wireway.protocol.Packet;
import xyz.wireway.protocol.PacketRegistry;

import java.util.function.BiConsumer;

public class PacketChannelContext {

    private PacketRegistry packetRegistry;
    private BiConsumer<Packet, Integer> onPacketReceivedAtSubChannel;

    public PacketChannelContext(PacketRegistry packetRegistry, BiConsumer<Packet, Integer> onPacketReceivedAtSubChannel) {
        this.packetRegistry = packetRegistry;
        this.onPacketReceivedAtSubChannel = onPacketReceivedAtSubChannel;
    }

    public PacketRegistry getPacketRegistry() {
        return packetRegistry;
    }

    public BiConsumer<Packet, Integer> getOnPacketReceivedAtSubChannel() {
        return onPacketReceivedAtSubChannel;
    }
}
