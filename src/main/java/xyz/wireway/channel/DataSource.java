package xyz.wireway.channel;

import java.nio.ByteBuffer;

/**
 * Outbound byte producer drained by the transmit loop. {@link #available} and
 * {@link #read} are called from the transmit thread only; implementations whose
 * producers add data from other threads must synchronize internally.
 */
public interface DataSource {

    int available();

    /** Copies exactly {@code length} bytes into {@code dst}; {@code length <= available()}. */
    void read(ByteBuffer dst, int length);

    /** True once no further data will ever become available. */
    boolean isComplete();
}
