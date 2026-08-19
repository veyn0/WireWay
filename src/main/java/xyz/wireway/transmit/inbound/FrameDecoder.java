package xyz.wireway.transmit.inbound;

import xyz.wireway.transport.TransportListener;
import xyz.wireway.util.ComposedBuffer;
import xyz.wireway.wire.Frame;
import xyz.wireway.wire.FrameFragment;
import xyz.wireway.wire.LengthPrefixed;

import java.nio.ByteBuffer;

/** Reassembles frames from the raw byte stream and feeds their fragments to the demux. */
public final class FrameDecoder implements TransportListener {

    private final ComposedBuffer buffer = new ComposedBuffer();
    private final InboundDemux demux;

    public FrameDecoder(InboundDemux demux) {
        this.demux = demux;
    }

    @Override
    public void onReceive(ByteBuffer data) {
        buffer.add(data);
        while (LengthPrefixed.hasRecord(buffer)) {
            Frame frame = Frame.parse(LengthPrefixed.takeRecord(buffer));
            for (FrameFragment fragment : frame.fragments()) {
                demux.accept(fragment);
            }
        }
    }
}
