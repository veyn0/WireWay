package xyz.wireway.transport.listener;

import xyz.wireway.transport.TransportListener;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class DebugtransportListener implements TransportListener {

    @Override
    public void onReceive(ByteBuffer data) {
//        ByteBuffer buffer = data.duplicate();
//        System.out.println("[DEBUG] TransportListener received:");
//        byte[] bytes = new byte[buffer.remaining()];
//        buffer.get(bytes);
//        System.out.println(Arrays.toString(bytes));
    }

}
