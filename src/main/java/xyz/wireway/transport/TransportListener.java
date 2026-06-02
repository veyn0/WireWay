package xyz.wireway.transport;

import java.nio.ByteBuffer;

public interface TransportListener {

    public void onReceive(ByteBuffer data);

}
