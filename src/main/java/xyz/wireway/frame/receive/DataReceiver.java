package xyz.wireway.frame.receive;

import java.nio.ByteBuffer;

public interface DataReceiver {

    void onReceive(ByteBuffer data);

    void close();

}
