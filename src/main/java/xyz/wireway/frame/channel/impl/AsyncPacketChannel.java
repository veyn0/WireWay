package xyz.wireway.frame.channel.impl;

import xyz.wireway.frame.channel.Channel;
import xyz.wireway.frame.channel.ChannelId;
import xyz.wireway.frame.channel.ComposedBufferBase;
import xyz.wireway.protocol.Packet;
import xyz.wireway.protocol.PacketRegistry;
import xyz.wireway.service.asyncpacketstream.AsyncPacketChannelContext;
import xyz.wireway.util.ProtocolUtils;

@ChannelId("xyz.wireway.syste.channel.asyncpacketchannel")
public class AsyncPacketChannel extends ComposedBufferBase implements Channel<AsyncPacketChannelContext> {

    private AsyncPacketChannelContext context;

    private int subId;

    public AsyncPacketChannel(){}

    public AsyncPacketChannel(Packet p, PacketRegistry packetRegistry){
        composedBuffer.add(Packet.getData(p, packetRegistry));
    }

    @Override
    protected void postWrite() {
        while (ProtocolUtils.canRead(composedBuffer)){
            Packet p = Packet.read(composedBuffer, context.getPacketRegistry());
            context.getOnPacketReceive().accept(p);
        }
    }

    @Override
    public void inject(AsyncPacketChannelContext context) {
        this.context = context;
    }


    @Override
    protected void postClose() {

    }


    @Override
    public boolean isExhausted() {
        return composedBuffer.remaining()<=0;
    }

}
