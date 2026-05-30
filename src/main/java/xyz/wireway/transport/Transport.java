package xyz.wireway.transport;

public interface Transport {

    public void send(byte[] content);

    public void addListener(TransportListener listener);

    public ConnectionState getConnectionState();

    public void close();

}
