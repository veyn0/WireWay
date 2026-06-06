package xyz.wireway.service;

import xyz.wireway.protocol.Packet;
import xyz.wireway.service.packetstream.PacketListener;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class PacketChannel {

    private final BiConsumer<Packet, Consumer<Packet>> onSendPacket;
    private final Consumer<PacketListener> onSetListener;

    public PacketChannel(BiConsumer<Packet, Consumer<Packet>> onSendPacket, Consumer<PacketListener> onSetListener) {
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
