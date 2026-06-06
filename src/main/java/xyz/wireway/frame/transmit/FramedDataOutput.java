package xyz.wireway.frame.transmit;

import xyz.wireway.frame.channel.ChannelRegistry;
import xyz.wireway.frame.Frame;
import xyz.wireway.frame.FrameFragment;
import xyz.wireway.transport.Transport;
import xyz.wireway.util.IdAllocator;
import xyz.wireway.util.VarInt;

import java.nio.ByteBuffer;
import java.util.*;

public class FramedDataOutput {

    private final Deque<OutgoingChannelWrapper> dataSources = new ArrayDeque<>();

    private final IdAllocator channelIdAllocator;

    private final Transport transport;

    private final ChannelRegistry channelRegistry;

    private static final int estimatedMaxFragmentHeaderSize = 11; // 5 + 1 + 5 + 5 (in case of it being the first fragment currently the channelId has to be sent fully within the first transmitted fragment.)

    private final int maxFrameFragmentLength;

    private final int maxFrameLength;

    public FramedDataOutput(Transport transport, ChannelRegistry channelRegistry, int maxFrameFragmentLength, int maxFrameLength) {
        if(maxFrameFragmentLength<16) throw new IllegalArgumentException("maxFrameFragmentLength must be at least 8");
        if(maxFrameLength<32) throw new IllegalArgumentException("maxFrameLength must be at least 12");
        if(maxFrameLength<(maxFrameFragmentLength+estimatedMaxFragmentHeaderSize)) throw new IllegalArgumentException("maxFrameLength cannot be less than 11 + fragmentlength");
        if(transport==null) throw new IllegalArgumentException("Transport cannot be null");
        this.maxFrameFragmentLength = maxFrameFragmentLength;
        this.maxFrameLength = maxFrameLength;
        this.channelIdAllocator = new IdAllocator();
        this.transport = transport;
        this.channelRegistry = channelRegistry;
        startSending();
    }

    public void addChannel(OutgoingChannelWrapper outgoingChannelWrapper){
        int id = channelIdAllocator.allocate();
        outgoingChannelWrapper.setChannelId(id);
        dataSources.add(outgoingChannelWrapper);
    }

    private void startSending(){
        new Thread(() ->{
            while (true) {
                sendFrame();
                try {
                    Thread.sleep(100);
                }catch (Exception e){

                }
            }
        }).start();
    }

    public void sendFrame(){
        //TODO: find more efficient way to create final bytebuffer of Frame.
        Frame currentFrame = buildNextFrame();
        if(currentFrame.getFragments().isEmpty()) return;
        int length= currentFrame.length();

        ByteBuffer data = ByteBuffer.allocateDirect(length);
        currentFrame.write(data);
        data.flip();
        // TODO: check for transport connectionstate
        transport.send(data);
    }

    private Frame buildNextFrame(){
        List<FrameFragment> fragments = new ArrayList<>();
        int remainingSize = maxFrameLength;
        boolean progressed = true;
        while ((progressed && remainingSize > 0 )){
            progressed = false;
            int passSize = dataSources.size();
            for(int i = 0; i < passSize; i++){
                OutgoingChannelWrapper outgoingChannelWrapper = dataSources.pollFirst();
                int maxSize = Math.min(maxFrameFragmentLength, remainingSize - estimatedMaxFragmentHeaderSize);
                if(maxSize > 0 && outgoingChannelWrapper.availableBytes()>0){
                    int channelId = outgoingChannelWrapper.getChannelId();
                    int chunkSize = Math.min(outgoingChannelWrapper.availableBytes(), maxSize);
                    ByteBuffer chunk = ByteBuffer.allocateDirect(chunkSize);
                    boolean sentBefore =  outgoingChannelWrapper.hasSentBefore();
                    outgoingChannelWrapper.read(chunk, chunkSize);
                    byte flags = outgoingChannelWrapper.getFlags(sentBefore);
                    FrameFragment result = new FrameFragment(channelId, flags, chunk.flip());
                    fragments.add(result);
                    remainingSize -= result.length();
                    progressed = true;
                }
                if(outgoingChannelWrapper.isExhausted()){
                    channelIdAllocator.release(outgoingChannelWrapper.getChannelId());
                }
                else {
                    dataSources.addLast(outgoingChannelWrapper);
                }
            }
        }
        return new Frame(fragments);
    }





}
