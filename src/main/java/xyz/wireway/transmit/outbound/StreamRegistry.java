package xyz.wireway.transmit.outbound;

import xyz.wireway.channel.DataSource;
import xyz.wireway.util.IdAllocator;
import xyz.wireway.wire.StreamHeader;

import java.util.ArrayDeque;

/** Allocates stream ids and holds the round-robin queue of open outbound streams. */
public final class StreamRegistry {

    private final IdAllocator streamIds = new IdAllocator();
    private final ArrayDeque<OutboundStream> queue = new ArrayDeque<>();

    public synchronized OutboundStream open(StreamHeader header, DataSource source) {
        OutboundStream stream = new OutboundStream(streamIds.allocate(), header, source);
        queue.addLast(stream);
        return stream;
    }

    synchronized OutboundStream poll() {
        return queue.pollFirst();
    }

    synchronized void requeue(OutboundStream stream) {
        queue.addLast(stream);
    }

    synchronized void release(OutboundStream stream) {
        streamIds.release(stream.streamId());
    }

    synchronized int size() {
        return queue.size();
    }

    synchronized boolean hasPending() {
        for (OutboundStream stream : queue) {
            if (stream.available() > 0) return true;
        }
        return false;
    }
}
