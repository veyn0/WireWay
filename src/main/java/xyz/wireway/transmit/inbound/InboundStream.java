package xyz.wireway.transmit.inbound;

import xyz.wireway.channel.ChannelRegistry;
import xyz.wireway.channel.DataSink;
import xyz.wireway.util.ComposedBuffer;
import xyz.wireway.wire.StreamHeader;

import java.nio.ByteBuffer;

/** Buffers one incoming stream until its header is complete, then forwards to the sink. */
final class InboundStream {

    private final ChannelRegistry channels;
    private ComposedBuffer headerBuffer = new ComposedBuffer(16);
    private DataSink sink;

    InboundStream(ChannelRegistry channels) {
        this.channels = channels;
    }

    void write(ByteBuffer data) {
        if (sink != null) {
            sink.write(data);
            return;
        }
        headerBuffer.add(data);
        StreamHeader header = StreamHeader.tryDecode(headerBuffer);
        if (header == null) return;
        sink = channels.createSink(header.channelTypeId(), header.endpointId());
        if (headerBuffer.remaining() > 0) {
            sink.write(headerBuffer.get());
        }
        headerBuffer = null;
    }

    void close() {
        if (sink != null) sink.close();
    }
}
