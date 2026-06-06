package xyz.wireway.frame.channel;

import xyz.wireway.protocol.Packet;
import xyz.wireway.service.stream.PacketChannelContext;
import xyz.wireway.util.ProtocolUtils;

@ChannelId("xyz.wireway.system.channel.sequentialpacketchannel")
public class SequentialPacketChannel extends ComposedBufferBase implements Channel<PacketChannelContext> {

    private PacketChannelContext context;

    private int subId;

    public SequentialPacketChannel() {
    }

    public void addPacket(Packet p){
        composedBuffer.add(Packet.getData(p, context.getPacketRegistry()));
    }

    @Override
    protected void postWrite() {
        while (ProtocolUtils.canRead(composedBuffer)){
            Packet p = Packet.read(composedBuffer, context.getPacketRegistry());
            context.getOnPacketReceivedAtSubChannel().accept(p, subId);
        }
    }

    @Override
    protected void postClose() {

    }

    @Override
    public boolean isExhausted() {
        return false;
    }

    @Override
    public void inject(PacketChannelContext context) {
        this.context = context;
    }

    @Override
    public void setSubId(int subId) {
        this.subId = subId;
    }

    @Override
    public int getSubId() {
        return subId;
    }


}
