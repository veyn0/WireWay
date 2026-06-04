package xyz.wireway.transport;

import java.nio.ByteBuffer;

public interface Transport {

    void send(ByteBuffer data);

    void addListener(TransportListener listener);

    ConnectionState getConnectionState();

    void close();

}
