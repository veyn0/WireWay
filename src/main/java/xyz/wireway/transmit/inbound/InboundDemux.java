package xyz.wireway.transmit.inbound;

import xyz.wireway.channel.ChannelRegistry;
import xyz.wireway.wire.FrameFragment;
import xyz.wireway.wire.ProtocolException;

import java.util.HashMap;
import java.util.Map;

/**
 * Routes fragments to their inbound stream by stream id. Confined to the
 * transport receive thread.
 */
public final class InboundDemux {

    private final ChannelRegistry channels;
    private final Map<Integer, InboundStream> streamsById = new HashMap<>();

    public InboundDemux(ChannelRegistry channels) {
        this.channels = channels;
    }

    public void accept(FrameFragment fragment) {
        int streamId = fragment.streamId();
        InboundStream stream;
        if (fragment.isStart()) {
            stream = new InboundStream(channels);
            if (streamsById.putIfAbsent(streamId, stream) != null) {
                throw new ProtocolException("start fragment for already open stream " + streamId);
            }
        } else {
            stream = streamsById.get(streamId);
            if (stream == null) {
                throw new ProtocolException("fragment for unknown stream " + streamId);
            }
        }
        stream.write(fragment.data());
        if (fragment.isEnd()) {
            streamsById.remove(streamId);
            stream.close();
        }
    }
}
