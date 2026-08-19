package xyz.wireway.channel;

import java.nio.ByteBuffer;

/**
 * Inbound byte consumer. Called from the transport receive thread only; the passed
 * buffer is only valid for the duration of the call and must be copied if retained.
 */
public interface DataSink {

    void write(ByteBuffer data);

    /** The peer closed the stream; no further {@link #write} calls follow. */
    void close();
}
