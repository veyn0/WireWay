package xyz.wireway.service;

import xyz.wireway.frame.channel.ChannelRegistry;
import xyz.wireway.frame.channel.ChannelSet;
import xyz.wireway.frame.channel.SequentialPacketChannel;
import xyz.wireway.frame.transmit.FramedDataInput;
import xyz.wireway.frame.transmit.FramedDataOutput;
import xyz.wireway.frame.transmit.OutgoingChannelWrapper;
import xyz.wireway.protocol.PacketRegistry;
import xyz.wireway.service.packetstream.PacketChannelContext;
import xyz.wireway.service.packetstream.PacketListener;
import xyz.wireway.service.packetstream.PacketStream;
import xyz.wireway.transport.Transport;
import xyz.wireway.transport.listener.DebugtransportListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WireWay {

    private Transport transport;

    private FramedDataInput framedDataInput;

    private FramedDataOutput framedDataOutput;

    private final PacketRegistry packetRegistry;

    private ChannelRegistry channelRegistry;

    private PacketChannelController packetChannelController;

    private PacketChannelContext packetChannelContext;

    public WireWay(Transport transport, PacketRegistry packetRegistry, int maxFrameLen, int maxFragmentLen){
        this.packetRegistry = packetRegistry;
        this.transport = transport;

        ChannelSet channelSet = new ChannelSet();
        packetChannelContext =  new PacketChannelContext(packetRegistry, this::onPacketReceive);
        channelSet.register(SequentialPacketChannel.class,packetChannelContext);

        this.channelRegistry = new ChannelRegistry(channelSet);
        this.framedDataInput = new FramedDataInput(channelRegistry);
        this.transport.addListener(framedDataInput);
        this.transport.addListener(new DebugtransportListener());
        this.framedDataOutput = new FramedDataOutput(this.transport, this.channelRegistry, maxFragmentLen, maxFrameLen);

        setupPacketChannelController();

    }

    private void onPacketReceive(PacketInfo packetInfo){
        packetChannelController.onPacketReceive(packetInfo);
    }

    private void setupPacketChannelController(){
        this.packetChannelController = new PacketChannelController(packetRegistry, framedDataOutput, channelRegistry, packetChannelContext );
    }

    public PacketChannel createPacketChannel(int id){
        return packetChannelController.createPacketChannel(id);
    }

}
