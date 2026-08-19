package xyz.wireway.transmit.outbound;

import xyz.wireway.channel.DataSource;
import xyz.wireway.wire.FrameFragment;
import xyz.wireway.wire.StreamHeader;

import java.nio.ByteBuffer;

/**
 * One outgoing stream: a data source plus its stream id and pending header bytes.
 * Mutated by the transmit thread only.
 */
public final class OutboundStream {

    private final int streamId;
    private final DataSource source;
    private ByteBuffer header;
    private boolean started;

    OutboundStream(int streamId, StreamHeader header, DataSource source) {
        this.streamId = streamId;
        this.header = header.encode();
        this.source = source;
    }

    public int streamId() {
        return streamId;
    }

    int available() {
        return (header == null ? 0 : header.remaining()) + source.available();
    }

    boolean isFinished() {
        return header == null && source.isComplete() && source.available() == 0;
    }

    /** Carves the next fragment of at most {@code maxPayload} bytes; call only when {@code available() > 0}. */
    FrameFragment take(int maxPayload) {
        ByteBuffer payload = ByteBuffer.allocate(Math.min(maxPayload, available()));
        boolean start = !started;
        started = true;
        if (header != null) {
            int fromHeader = Math.min(payload.remaining(), header.remaining());
            int limit = header.limit();
            header.limit(header.position() + fromHeader);
            payload.put(header);
            header.limit(limit);
            if (!header.hasRemaining()) header = null;
        }
        if (payload.hasRemaining()) {
            source.read(payload, payload.remaining());
        }
        payload.flip();
        return new FrameFragment(streamId, FrameFragment.encodeFlags(start, isFinished()), payload);
    }
}
