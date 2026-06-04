package xyz.wireway.frame.receive;

import xyz.wireway.frame.Frame;
import xyz.wireway.transport.TransportListener;
import xyz.wireway.util.ComposedBuffer;

import java.nio.ByteBuffer;

public class FramedDataInput implements TransportListener {

    private ComposedBuffer composedBuffer = new ComposedBuffer();

    @Override
    public void onReceive(ByteBuffer data) {
        composedBuffer.add(data);
    }

    private void handleIncomingFrames(){
        while (Frame.canRead( composedBuffer)){
            System.out.println("reading Frame");

            Frame f = Frame.read(composedBuffer);

        }
    }

}
