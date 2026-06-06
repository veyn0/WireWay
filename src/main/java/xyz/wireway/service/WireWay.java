package xyz.wireway.service;

import xyz.wireway.frame.datasource.DataSourceRegistry;
import xyz.wireway.frame.datasource.Provider;
import xyz.wireway.frame.datasource.SequentialPacketDataSource;
import xyz.wireway.frame.transmit.FramedDataInput;
import xyz.wireway.frame.transmit.FramedDataOutput;
import xyz.wireway.protocol.Packet;
import xyz.wireway.protocol.PacketRegistry;
import xyz.wireway.protocol.packet.HeartBeatPacket;
import xyz.wireway.service.channel.SequentialPacketChannel;
import xyz.wireway.transport.Transport;
import xyz.wireway.transport.listener.DebugtransportListener;

public class WireWay implements Provider {

    private Transport transport;

    private FramedDataInput framedDataInput;

    private FramedDataOutput framedDataOutput;

    private final PacketRegistry packetRegistry;

    private DataSourceRegistry dataSourceRegistry;

    public WireWay(Transport transport, DataSourceRegistry dataSourceRegistry, PacketRegistry packetRegistry, int maxFrameLen, int maxFragmentLen){
        this.packetRegistry = packetRegistry;
        this.transport = transport;
        dataSourceRegistry.setProvider(this);
        this.dataSourceRegistry = dataSourceRegistry;
        framedDataInput = new FramedDataInput(dataSourceRegistry);
        this.transport.addListener(framedDataInput);
        this.transport.addListener(new DebugtransportListener());
        framedDataOutput = new FramedDataOutput(transport, dataSourceRegistry, maxFragmentLen, maxFrameLen);
    }

    @Override
    public void onPacketReceive(Packet p) {
        if(p instanceof HeartBeatPacket packet) {
            System.out.println("Heartbeat: " + packet.getId() + " Time: " + packet.getTimestamp());
        }
    }

    @Override
    public PacketRegistry getPacketRegistry() {
        return packetRegistry;
    }

    public SequentialPacketChannel createSequentialPacketChannel(){
        if(!(dataSourceRegistry.createDataSource(3) instanceof SequentialPacketDataSource dataSource)) return null;
        SequentialPacketChannel channel = new SequentialPacketChannel(dataSource);
        framedDataOutput.addDataSource(dataSource);
        return channel;
    }
}
