package xyz.wireway.service;

import xyz.wireway.frame.channel.ChannelRegistry;
import xyz.wireway.frame.channel.ChannelSet;
import xyz.wireway.frame.channel.impl.AsyncPacketChannel;
import xyz.wireway.frame.channel.impl.SequentialPacketChannel;
import xyz.wireway.frame.transmit.FramedDataInput;
import xyz.wireway.frame.transmit.FramedDataOutput;
import xyz.wireway.frame.transmit.OutgoingChannelWrapper;
import xyz.wireway.protocol.Packet;
import xyz.wireway.protocol.PacketRegistry;
import xyz.wireway.service.asyncpacketstream.AsyncPacketChannelContext;
import xyz.wireway.service.asyncpacketstream.AsyncPacketListener;
import xyz.wireway.service.packetstream.PacketStream;
import xyz.wireway.service.packetstream.PacketChannelContext;
import xyz.wireway.service.packetstream.PacketStreamController;
import xyz.wireway.service.packetstream.PacketInfo;
import xyz.wireway.transport.Transport;
import xyz.wireway.transport.listener.DebugtransportListener;

import java.util.ArrayList;
import java.util.List;

public class WireWay {

    private Transport transport;

    private FramedDataInput framedDataInput;

    private FramedDataOutput framedDataOutput;

    private final PacketRegistry packetRegistry;

    private ChannelRegistry channelRegistry;

    private PacketStreamController packetStreamController;

    private PacketChannelContext packetChannelContext;

    private AsyncPacketChannelContext asyncPacketChannelContext;

    private List<AsyncPacketListener> asyncPacketListeners = new ArrayList<>();

    public WireWay(Transport transport, PacketRegistry packetRegistry, ChannelSet channelSet,  int maxFrameLen, int maxFragmentLen){
        this.packetRegistry = packetRegistry;
        this.transport = transport;

        packetChannelContext =  new PacketChannelContext(packetRegistry, this::onPacketReceiveOnChannel);
        asyncPacketChannelContext = new AsyncPacketChannelContext(packetRegistry, this::onPacketReceiveAsync);
        channelSet.register(SequentialPacketChannel.class,packetChannelContext);
        channelSet.register(AsyncPacketChannel.class, asyncPacketChannelContext);

        this.channelRegistry = new ChannelRegistry(channelSet);
        this.framedDataInput = new FramedDataInput(channelRegistry);
        this.transport.addListener(framedDataInput);
        this.transport.addListener(new DebugtransportListener());
        this.framedDataOutput = new FramedDataOutput(this.transport, this.channelRegistry, maxFragmentLen, maxFrameLen);

        setupPacketChannelController();

    }

    private void onPacketReceiveOnChannel(PacketInfo packetInfo){
        packetStreamController.onPacketReceive(packetInfo);
    }

    private void onPacketReceiveAsync(Packet p){
        for(AsyncPacketListener listener : asyncPacketListeners){
            listener.onPacketReceive(p);
        }
    }

    private void setupPacketChannelController(){
        this.packetStreamController = new PacketStreamController(packetRegistry, framedDataOutput, channelRegistry, packetChannelContext );
    }

    public PacketStream createPacketChannel(int id){
        return packetStreamController.createPacketChannel(id);
    }

    public void addAsyncPacketChannelListener(AsyncPacketListener listener){
        asyncPacketListeners.add(listener);
    }

    public void sendPacketAsync(Packet packet){
        AsyncPacketChannel channel = new AsyncPacketChannel(packet, packetRegistry);
        channel.setSubId(0);
        channel.inject(asyncPacketChannelContext);
        framedDataOutput.addChannel(new OutgoingChannelWrapper(channel, channelRegistry));
    }

}
