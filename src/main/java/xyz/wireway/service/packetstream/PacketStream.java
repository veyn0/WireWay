package xyz.wireway.service.packetstream;

import xyz.wireway.protocol.Packet;

import java.util.function.Consumer;

public class PacketStream {

    private Consumer<Packet> onPacketSend;
    private Consumer<PacketListener> onListenerAdded;

    public PacketStream(Consumer<Packet> onPacketSend, Consumer<PacketListener> onListenerAdded) {
        this.onPacketSend = onPacketSend;
        this.onListenerAdded = onListenerAdded;
    }

    public void sendPacket(Packet packet){
        this.onPacketSend.accept(packet);
    }

    public void addListener(PacketListener listener){
        onListenerAdded.accept(listener);
    }

}
