package xyz.wireway.service;

import xyz.wireway.channel.DataSink;
import xyz.wireway.protocol.Packet;
import xyz.wireway.protocol.PacketRegistry;
import xyz.wireway.util.ComposedBuffer;
import xyz.wireway.wire.LengthPrefixed;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.Executor;

/** Inbound side of the fire-and-forget channel: plain packet records. */
final class AsyncPacketSink implements DataSink {

    private final ComposedBuffer buffer = new ComposedBuffer();
    private final PacketRegistry packets;
    private final Executor dispatcher;
    private final List<AsyncPacketListener> listeners;

    AsyncPacketSink(PacketRegistry packets, Executor dispatcher, List<AsyncPacketListener> listeners) {
        this.packets = packets;
        this.dispatcher = dispatcher;
        this.listeners = listeners;
    }

    @Override
    public void write(ByteBuffer data) {
        buffer.add(data);
        while (LengthPrefixed.hasRecord(buffer)) {
            Packet packet = Packet.readRecord(buffer, packets);
            dispatcher.execute(() -> {
                for (AsyncPacketListener listener : listeners) {
                    listener.onPacketReceive(packet);
                }
            });
        }
    }

    @Override
    public void close() {}
}
