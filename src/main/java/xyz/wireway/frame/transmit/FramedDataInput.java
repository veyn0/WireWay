package xyz.wireway.frame.transmit;

import xyz.wireway.frame.Frame;
import xyz.wireway.frame.FrameFragment;
import xyz.wireway.frame.channel.ChannelRegistry;
import xyz.wireway.transport.TransportListener;
import xyz.wireway.util.ComposedBuffer;
import xyz.wireway.util.ProtocolUtils;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FramedDataInput implements TransportListener {

    private ComposedBuffer composedBuffer = new ComposedBuffer();

    private final ChannelRegistry channelRegistry;

    private Map<Integer, IncomingChannelWrapper> channelsById = new ConcurrentHashMap<>();

    public FramedDataInput(ChannelRegistry channelRegistry) {
        this.channelRegistry = channelRegistry;
    }

    @Override
    public void onReceive(ByteBuffer data) {
        composedBuffer.add(data);
        handleIncomingFrames();
    }

    private void handleIncomingFrames(){
        while (ProtocolUtils.canRead( composedBuffer)){

            Frame frame = Frame.read(composedBuffer);
            for(FrameFragment fragment : frame.getFragments()){
                handleFrameFragment(fragment);
            }
        }
    }

    private void handleFrameFragment(FrameFragment f){
        int dataId = f.getDataId();
        if(f.isStart()){
            channelsById.put(dataId, new IncomingChannelWrapper(channelRegistry));
        }
        IncomingChannelWrapper channel = channelsById.get(dataId);
        channel.write(f.getData());
        if(f.isEnd()) channel.close();
    }

}
