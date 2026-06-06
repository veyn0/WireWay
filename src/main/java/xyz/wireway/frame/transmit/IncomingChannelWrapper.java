package xyz.wireway.frame.transmit;

import xyz.wireway.frame.channel.Channel;
import xyz.wireway.frame.channel.ChannelRegistry;
import xyz.wireway.util.ComposedBuffer;
import xyz.wireway.util.VarInt;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public class IncomingChannelWrapper {

    private ChannelRegistry channelRegistry;

    private ComposedBuffer metaData = new ComposedBuffer(16);

    private Channel<?> channel;


    public IncomingChannelWrapper(ChannelRegistry channelRegistry) {
        this.channelRegistry = channelRegistry;
    }

    public void write(ByteBuffer buffer) {
        if (channel != null) {
            channel.write(buffer);
            return;
        }
        metaData.add(buffer);

        ByteBuffer data = metaData.peek(metaData.remaining());

        try {
            int channelTypeId = VarInt.readVarIntSafe(data);
            int parsedSubId = VarInt.readVarIntSafe(data);

            this.channel = channelRegistry.createChannel(channelTypeId);
            this.channel.setSubId(parsedSubId);

            if (data.hasRemaining()) {
                channel.write(data);
            }
            metaData = null;
        } catch (BufferUnderflowException e) {
            throw new RuntimeException(e);
        }
    }

    public void close(){
        if (channel != null) channel.close();
    };

}
