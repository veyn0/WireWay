package xyz.wireway.transmit.outbound;

import xyz.wireway.wire.Frame;
import xyz.wireway.wire.FrameFragment;
import xyz.wireway.wire.LengthPrefixed;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds frames by taking round-robin chunks from the pending streams within the
 * configured byte budgets. Runs on the transmit thread only.
 */
public final class FrameAssembler {

    private final StreamRegistry streams;
    private final int maxFrameLength;
    private final int maxFragmentLength;

    public FrameAssembler(StreamRegistry streams, int maxFrameLength, int maxFragmentLength) {
        if (maxFragmentLength < 16) {
            throw new IllegalArgumentException("maxFragmentLength must be at least 16");
        }
        int minFrameLength = maxFragmentLength + FrameFragment.MAX_HEADER_BYTES + LengthPrefixed.MAX_PREFIX_BYTES;
        if (maxFrameLength < minFrameLength) {
            throw new IllegalArgumentException(
                    "maxFrameLength must be at least " + minFrameLength + " for maxFragmentLength " + maxFragmentLength);
        }
        this.streams = streams;
        this.maxFrameLength = maxFrameLength;
        this.maxFragmentLength = maxFragmentLength;
    }

    /** Returns the next frame within the budgets, or null when no stream has data. */
    public Frame next() {
        List<FrameFragment> fragments = new ArrayList<>();
        int remaining = maxFrameLength - LengthPrefixed.MAX_PREFIX_BYTES;
        boolean progressed = true;
        while (progressed) {
            progressed = false;
            int pass = streams.size();
            for (int i = 0; i < pass; i++) {
                OutboundStream stream = streams.poll();
                if (stream == null) break;
                int budget = Math.min(maxFragmentLength, remaining - FrameFragment.MAX_HEADER_BYTES);
                if (budget > 0 && stream.available() > 0) {
                    FrameFragment fragment = stream.take(budget);
                    fragments.add(fragment);
                    remaining -= fragment.encodedLength();
                    progressed = true;
                }
                if (stream.isFinished()) {
                    streams.release(stream);
                } else {
                    streams.requeue(stream);
                }
            }
        }
        return fragments.isEmpty() ? null : new Frame(fragments);
    }
}
