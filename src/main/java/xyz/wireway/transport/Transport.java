package xyz.wireway.transport;

import java.nio.ByteBuffer;

public interface Transport {

    public void send(ByteBuffer data);

    public void addListener(TransportListener listener);

    public ConnectionState getConnectionState();

    public void close();

}
