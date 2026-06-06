package xyz.wireway.service;

import xyz.wireway.frame.channel.ChannelRegistry;
import xyz.wireway.frame.channel.ChannelSet;
import xyz.wireway.frame.channel.SequentialPacketChannel;
import xyz.wireway.frame.transmit.FramedDataInput;
import xyz.wireway.frame.transmit.FramedDataOutput;
import xyz.wireway.frame.transmit.OutgoingChannelWrapper;
import xyz.wireway.protocol.PacketRegistry;
import xyz.wireway.service.stream.PacketChannelContext;
import xyz.wireway.service.stream.PacketListener;
import xyz.wireway.service.stream.PacketStream;
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

    private Map<Integer, SequentialPacketChannel> outgoingPacketStream = new ConcurrentHashMap<>();

    private Map<Integer, List<PacketListener>> incomingPacketListener = new ConcurrentHashMap<>();

    public WireWay(Transport transport, ChannelRegistry channelRegistry, PacketRegistry packetRegistry, int maxFrameLen, int maxFragmentLen){
        this.packetRegistry = packetRegistry;
        this.transport = transport;
        channelRegistry = createChannelRegistry();
        this.channelRegistry = channelRegistry;
        this.framedDataInput = new FramedDataInput(channelRegistry);
        this.transport.addListener(framedDataInput);
        this.transport.addListener(new DebugtransportListener());
        this.framedDataOutput = new FramedDataOutput(this.transport, this.channelRegistry, maxFragmentLen, maxFrameLen);
    }

    private ChannelRegistry createChannelRegistry(){
        ChannelSet channelSet = new ChannelSet();
        channelSet.register(SequentialPacketChannel.class, createChannelContext());
        return new ChannelRegistry(channelSet);
    }

    private PacketChannelContext createChannelContext(){
        return new PacketChannelContext(
                packetRegistry,
                (packet, id) ->{
                    if( incomingPacketListener.containsKey(id)){
                        for(PacketListener listener : incomingPacketListener.get(id)){
                            listener.onPacketReceive(packet);
                        }
                    }
                }
        );
    }

    public PacketStream createPacketStream(int id){
        SequentialPacketChannel channel = new SequentialPacketChannel();
        channel.inject(createChannelContext());
        channel.setSubId(id);

        outgoingPacketStream.put(id, channel);
        framedDataOutput.addChannel(new OutgoingChannelWrapper(channel, channelRegistry));

        return  new PacketStream(
                channel::addPacket,
                packetListener -> {
                    if(!incomingPacketListener.containsKey(id)){
                        incomingPacketListener.put(id, new ArrayList<>());
                    }
                    incomingPacketListener.get(id).add(packetListener);
                }
        );
    }

}
