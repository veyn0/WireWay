package xyz.wireway.frame.receive;

import xyz.wireway.frame.Frame;
import xyz.wireway.frame.FrameFragment;
import xyz.wireway.transport.TransportListener;
import xyz.wireway.util.ComposedBuffer;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FramedDataInput implements TransportListener {

    private ComposedBuffer composedBuffer = new ComposedBuffer();

    private Map<Integer, DataReceiver> dataReceivers = new ConcurrentHashMap<>();

    @Override
    public void onReceive(ByteBuffer data) {
        composedBuffer.add(data);
        handleIncomingFrames();
    }

    private void handleIncomingFrames(){
        while (Frame.canRead( composedBuffer)){
            System.out.println("reading Frame");

            Frame frame = Frame.read(composedBuffer);
            for(FrameFragment fragment : frame.getFragments()){
                handleFrameFragment(fragment);
            }
        }
    }

    private void handleFrameFragment(FrameFragment f){
        int dataId = f.getDataId();
        if(f.isStart()) dataReceivers.put(dataId, new DataReceiver());
        DataReceiver dataReceiver = this.dataReceivers.get(dataId);
        dataReceiver.onReceive(f.getData());
        if(f.isEnd()) dataReceiver.close();
    }

}
