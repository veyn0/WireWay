package xyz.wireway.frame.send;

import xyz.wireway.Main;
import xyz.wireway.frame.Frame;
import xyz.wireway.frame.FrameFragment;
import xyz.wireway.frame.datasource.DataSource;
import xyz.wireway.frame.datasource.DataSourceInfo;
import xyz.wireway.transport.Transport;
import xyz.wireway.util.IdAllocator;
import xyz.wireway.util.VarInt;

import java.nio.ByteBuffer;
import java.util.*;

public class FramedDataOutput {

    private final Deque<DataSourceInfo> dataSources = new ArrayDeque<>();

    private final IdAllocator dataSourceIdAllocator;

    private final Transport transport;

    private static final int estimatedMaxFragmentHeaderSize = 11; // 5 + 1 + 5

    private final int maxFrameFragmentLength;

    private final int maxFrameLength;

    public FramedDataOutput(Transport transport, int maxFrameFragmentLength, int maxFrameLength) {
        if(maxFrameFragmentLength<1) throw new IllegalArgumentException("maxFrameFragmentLength must be at least 1");
        if(maxFrameLength<12) throw new IllegalArgumentException("maxFrameLength must be at least 12");
        if(maxFrameLength<(maxFrameFragmentLength+estimatedMaxFragmentHeaderSize)) throw new IllegalArgumentException("maxFrameLength cannot be less than 11 + fragmentlength");
        if(transport==null) throw new IllegalArgumentException("Transport cannot be null");
        this.maxFrameFragmentLength = maxFrameFragmentLength;
        this.maxFrameLength = maxFrameLength;
        this.dataSourceIdAllocator = new IdAllocator();
        this.transport = transport;
    }

    public void addDataSource(DataSource dataSource){
        int id = dataSourceIdAllocator.allocate();
        dataSources.add(new DataSourceInfo(id, dataSource));
    }

    private void removeDataSource(DataSourceInfo dataSourceInfo){
        dataSources.remove(dataSourceInfo);
        dataSourceIdAllocator.release(dataSourceInfo.getId());
    }

    public void sendFrame(){
        //TODO: find more efficient way to create final bytebuffer of Frame.
        Frame currentFrame = buildNextFrame();
        int length= currentFrame.length();
        ByteBuffer data = ByteBuffer.allocateDirect(length);
        currentFrame.write(data);
        transport.send(data);
    }

    private Frame buildNextFrame(){
        List<FrameFragment> fragments = new ArrayList<>();
        int remainingSize = maxFrameLength;
        boolean progressed = true;
        while ((progressed && remainingSize > 0 )|| fragments.isEmpty()){
            progressed = false;
            int passSize = dataSources.size();
            for(int i = 0; i < passSize; i++){
                DataSourceInfo dataSourceInfo = dataSources.pollFirst();
                DataSource dataSource = dataSourceInfo.getDataSource();
                int maxSize = Math.min(maxFrameFragmentLength, remainingSize - estimatedMaxFragmentHeaderSize);
                if(maxSize > 0 && dataSource.availableBytes()>0){
                    int dataId = dataSourceInfo.getId();
                    int chunkSize = Math.min(dataSource.availableBytes(), maxSize);
                    ByteBuffer chunk = ByteBuffer.allocateDirect(chunkSize);
                    dataSource.write(chunk, chunkSize);
                    byte flags = computeFlags(dataSourceInfo);
                    FrameFragment result = new FrameFragment(dataId, flags, chunk.flip());
                    fragments.add(result);
                    remainingSize -= result.length();
                    progressed = true;
                }
                if(dataSource.isExhausted()){
                    dataSourceIdAllocator.release(dataSourceInfo.getId());
                }
                else {
                    dataSources.addLast(dataSourceInfo);
                }
            }
        }
        return new Frame(fragments);
    }




    private byte computeFlags(DataSourceInfo dataSourceInfo){
        boolean isFirstFragment = !dataSourceInfo.isStartedSending();
        dataSourceInfo.startedSending();
        boolean isEnd = dataSourceInfo.getDataSource().isExhausted();
        byte result = 0;
        if(isFirstFragment) result |= 2;
        if(isEnd) result |= 1;
        return result;
    }


}
