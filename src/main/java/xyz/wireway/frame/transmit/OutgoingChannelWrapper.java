package xyz.wireway.frame.transmit;

import xyz.wireway.frame.FrameFragment;
import xyz.wireway.frame.channel.Channel;
import xyz.wireway.frame.channel.ChannelRegistry;
import xyz.wireway.util.VarInt;

import java.nio.ByteBuffer;

public class OutgoingChannelWrapper {

    private final Channel<?> channel;
    private int channelId;
    private ByteBuffer metaData;

    private boolean isStart = true;

    public OutgoingChannelWrapper(Channel<?> channel, ChannelRegistry channelRegistry) {
        this.channel = channel;
        int channelTypeId = channelRegistry.getChannelId(channel);
        int subId = channel.getSubId();
        this.metaData = ByteBuffer.allocate(VarInt.sizeOf(channelTypeId) + VarInt.sizeOf(subId));
        VarInt.writeVarInt(metaData, channelTypeId);
        VarInt.writeVarInt(metaData, subId);
        metaData.flip();
    }

    public void setChannelId(int channelId) {
        this.channelId = channelId;
    }

    public int getChannelId() {
        return channelId;
    }

    public byte getFlags(boolean hasSentBefore){
        return FrameFragment.encodeFlags(!hasSentBefore, (channel.isExhausted()&&availableBytes()<=0));
    }

    public Channel<?> getChannel() {
        return channel;
    }

    public int availableBytes() {
        return channel.availableBytes() + (metaData == null ? 0 : metaData.remaining());
    }

    public void read(ByteBuffer chunk, int length) {
        isStart = false;

        if (metaData != null && metaData.hasRemaining()) {
            int fromMeta = Math.min(length, metaData.remaining());
            int oldLimit = metaData.limit();
            metaData.limit(metaData.position() + fromMeta);
            chunk.put(metaData);
            metaData.limit(oldLimit);

            length -= fromMeta;

            if (!metaData.hasRemaining()) {
                metaData = null;
            }
        }

        if (length > 0) {
            channel.read(chunk, length);
        }
    }

    public boolean hasSentBefore(){
        return !isStart;
    }

    public boolean isExhausted(){
        return channel!=null&&channel.isExhausted()&&availableBytes()<=0;
    }
}