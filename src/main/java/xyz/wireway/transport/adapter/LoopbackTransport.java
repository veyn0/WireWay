package xyz.wireway.transport.adapter;

import xyz.wireway.transport.ConnectionState;
import xyz.wireway.transport.Transport;
import xyz.wireway.transport.TransportListener;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class LoopbackTransport implements Transport {

    private List<TransportListener> transportListeners = new ArrayList<>();

    private ConnectionState connectionState;

    public static LoopbackTransport connect(){
        return new LoopbackTransport();
    }

    private LoopbackTransport(){
        connectionState = ConnectionState.CONNECTED;
    }

    @Override
    public void send(ByteBuffer data) {
        if(connectionState==ConnectionState.CLOSED) throw new RuntimeException("Cannot send to already closed TransportChannel");
        for(TransportListener l : transportListeners){
            l.onReceive(data.duplicate());
        }
    }

    @Override
    public void addListener(TransportListener listener) {
        transportListeners.add(listener);
    }

    @Override
    public ConnectionState getConnectionState() {
        return connectionState;
    }

    @Override
    public void close() {
        this.connectionState = ConnectionState.CLOSED;
    }


}
