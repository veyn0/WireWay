package xyz.wireway.transport;

import java.nio.ByteBuffer;

public interface TransportListener {

    void onReceive(ByteBuffer data);

    default void onError(Throwable cause) {}

    default void onClosed() {}
}
