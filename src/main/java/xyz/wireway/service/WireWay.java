package xyz.wireway.service;

import xyz.wireway.frame.channel.ChannelRegistry;
import xyz.wireway.frame.transmit.FramedDataInput;
import xyz.wireway.frame.transmit.FramedDataOutput;
import xyz.wireway.protocol.Packet;
import xyz.wireway.protocol.PacketRegistry;
import xyz.wireway.transport.Transport;
import xyz.wireway.transport.listener.DebugtransportListener;

public class WireWay {

    private Transport transport;

    private FramedDataInput framedDataInput;

    private FramedDataOutput framedDataOutput;

    private final PacketRegistry packetRegistry;

    private ChannelRegistry channelRegistry;

    public WireWay(Transport transport, ChannelRegistry channelRegistry, PacketRegistry packetRegistry, int maxFrameLen, int maxFragmentLen){
        this.packetRegistry = packetRegistry;
        this.transport = transport;
        this.channelRegistry = channelRegistry;
        this.framedDataInput = new FramedDataInput(channelRegistry);
        this.transport.addListener(framedDataInput);
        this.transport.addListener(new DebugtransportListener());
        this.framedDataOutput = new FramedDataOutput(this.transport, this.channelRegistry, maxFragmentLen, maxFrameLen);
    }




}
