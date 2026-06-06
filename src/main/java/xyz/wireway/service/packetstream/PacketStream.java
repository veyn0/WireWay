package xyz.wireway.service.packetstream;

import xyz.wireway.protocol.Packet;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class PacketStream {

    private final BiConsumer<Packet, Consumer<Packet>> onSendPacket;
    private final Consumer<PacketListener> onSetListener;

    public PacketStream(BiConsumer<Packet, Consumer<Packet>> onSendPacket, Consumer<PacketListener> onSetListener) {
        this.onSendPacket = onSendPacket;
        this.onSetListener = onSetListener;
    }

    public void sendPacket(Packet packet, Consumer<Packet> onResponse){
        onSendPacket.accept(packet, onResponse);
    }

    public void setListener(PacketListener listener){
        onSetListener.accept(listener);
    }

}
