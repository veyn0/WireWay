package xyz.wireway.transport.adapter;

import xyz.wireway.transport.ConnectionState;
import xyz.wireway.transport.Transport;
import xyz.wireway.transport.TransportListener;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Test transport that echoes every send back to its own listeners. */
public class LoopbackTransport implements Transport {

    private final List<TransportListener> listeners = new CopyOnWriteArrayList<>();
    private volatile ConnectionState connectionState = ConnectionState.CONNECTED;

    public static LoopbackTransport connect() {
        return new LoopbackTransport();
    }

    private LoopbackTransport() {}

    @Override
    public void send(ByteBuffer data) {
        if (connectionState == ConnectionState.CLOSED) {
            throw new IllegalStateException("transport is closed");
        }
        for (TransportListener listener : listeners) {
            listener.onReceive(data.duplicate());
        }
    }

    @Override
    public void addListener(TransportListener listener) {
        listeners.add(listener);
    }

    @Override
    public void start() {}

    @Override
    public ConnectionState getConnectionState() {
        return connectionState;
    }

    @Override
    public void close() {
        if (connectionState == ConnectionState.CLOSED) return;
        connectionState = ConnectionState.CLOSED;
        for (TransportListener listener : listeners) {
            listener.onClosed();
        }
    }
}
