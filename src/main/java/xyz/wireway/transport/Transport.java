package xyz.wireway.transport;

import java.nio.ByteBuffer;

public interface Transport {

    /** Writes the buffer to the peer; throws when the transport is closed or the write fails. */
    void send(ByteBuffer data);

    /** Must be called before {@link #start()}; data received earlier would be lost. */
    void addListener(TransportListener listener);

    /** Begins delivering inbound data to the registered listeners. Idempotent. */
    void start();

    ConnectionState getConnectionState();

    /** Closes the underlying connection and notifies listeners. Idempotent. */
    void close();
}
