package xyz.wireway.service;

import xyz.wireway.channel.BufferSource;
import xyz.wireway.channel.ChannelRegistry;
import xyz.wireway.channel.DataSink;
import xyz.wireway.channel.ChannelSet;
import xyz.wireway.protocol.Packet;
import xyz.wireway.protocol.PacketRegistry;
import xyz.wireway.transmit.inbound.FrameDecoder;
import xyz.wireway.transmit.inbound.InboundDemux;
import xyz.wireway.transmit.outbound.FrameAssembler;
import xyz.wireway.transmit.outbound.StreamRegistry;
import xyz.wireway.transmit.outbound.TransmitLoop;
import xyz.wireway.transport.Transport;
import xyz.wireway.transport.TransportListener;
import xyz.wireway.wire.StreamHeader;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/** Entry point: wires transport, framing, channels and packet streams for one connection. */
public final class WireWay implements AutoCloseable {

    public static final String SEQUENTIAL_CHANNEL = "xyz.wireway.system.channel.sequential";
    public static final String ASYNC_CHANNEL = "xyz.wireway.system.channel.async";

    private final Transport transport;
    private final PacketRegistry packets;
    private final ChannelRegistry channels;
    private final StreamRegistry streams;
    private final TransmitLoop transmitLoop;
    private final ScheduledExecutorService dispatcher;
    private final PacketStreamController controller;
    private final List<AsyncPacketListener> asyncListeners = new CopyOnWriteArrayList<>();

    public WireWay(Transport transport, PacketRegistry packets, int maxFrameLength, int maxFragmentLength) {
        this(transport, packets, new ChannelSet(), maxFrameLength, maxFragmentLength);
    }

    public WireWay(Transport transport, PacketRegistry packets, ChannelSet userChannels,
                   int maxFrameLength, int maxFragmentLength) {
        this.transport = Objects.requireNonNull(transport, "transport");
        this.packets = Objects.requireNonNull(packets, "packets");
        this.dispatcher = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "wireway-dispatch");
            thread.setDaemon(true);
            return thread;
        });
        this.streams = new StreamRegistry();
        this.transmitLoop = new TransmitLoop(transport, streams,
                new FrameAssembler(streams, maxFrameLength, maxFragmentLength));

        ChannelSet systemChannels = new ChannelSet();
        systemChannels.register(SEQUENTIAL_CHANNEL, this::createSequentialSink);
        systemChannels.register(ASYNC_CHANNEL, endpointId -> new AsyncPacketSink(packets, dispatcher, asyncListeners));
        this.channels = new ChannelRegistry(systemChannels, userChannels);

        this.controller = new PacketStreamController(packets, dispatcher, this::openOutbox);

        transport.addListener(new FrameDecoder(new InboundDemux(channels)));
        transport.addListener(new TransportListener() {
            @Override
            public void onReceive(ByteBuffer data) {}

            @Override
            public void onClosed() {
                transmitLoop.close();
            }
        });
        transmitLoop.start();
        transport.start();
    }

    private DataSink createSequentialSink(int endpointId) {
        return controller.createSink(endpointId);
    }

    private PacketOutbox openOutbox(int endpointId) {
        PacketOutbox outbox = new PacketOutbox(transmitLoop::signal);
        streams.open(new StreamHeader(channels.typeId(SEQUENTIAL_CHANNEL), endpointId), outbox);
        return outbox;
    }

    /** Opens (or returns a view onto) the request/response stream for the given endpoint. */
    public PacketStream openStream(int endpointId) {
        return new PacketStream(controller, endpointId);
    }

    /** Sends a fire-and-forget packet over the async channel. */
    public void sendPacket(Packet packet) {
        streams.open(new StreamHeader(channels.typeId(ASYNC_CHANNEL), 0),
                new BufferSource(Packet.encodeRecord(packet, packets)));
        transmitLoop.signal();
    }

    public void addPacketListener(AsyncPacketListener listener) {
        asyncListeners.add(listener);
    }

    @Override
    public void close() {
        transmitLoop.close();
        transport.close();
        controller.shutdown();
        dispatcher.shutdown();
    }
}
