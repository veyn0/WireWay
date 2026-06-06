package xyz.wireway.service.channel;

import xyz.wireway.frame.datasource.SequentialPacketDataSource;
import xyz.wireway.protocol.Packet;

public class SequentialPacketChannel {

    private SequentialPacketDataSource sequentialPacketDataSource;

    public SequentialPacketChannel(SequentialPacketDataSource sequentialPacketDataSource) {
        this.sequentialPacketDataSource = sequentialPacketDataSource;
    }

    public void submitPacket(Packet p){
        sequentialPacketDataSource.addPacket(p);
    }


}
