package xyz.wireway.transport;

public interface Transport {

    public void send(byte[] content);

    public void setListener(TransportListener listener);

}
