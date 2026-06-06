package xyz.wireway.frame.transmit;

import xyz.wireway.frame.datasource.DataSourceRegistry;
import xyz.wireway.frame.Frame;
import xyz.wireway.frame.FrameFragment;
import xyz.wireway.frame.datasource.DataSource;
import xyz.wireway.transport.Transport;
import xyz.wireway.util.IdAllocator;
import xyz.wireway.util.VarInt;

import java.nio.ByteBuffer;
import java.util.*;

public class FramedDataOutput {

    private final Deque<DataSourceInfo> dataSources = new ArrayDeque<>();

    private final IdAllocator dataSourceIdAllocator;

    private final Transport transport;

    private final DataSourceRegistry dataSourceRegistry;

    private static final int estimatedMaxFragmentHeaderSize = 16; // 5 + 1 + 5 + 5 (in case of it being the first fragment currently the datasourceid has to be sent fully within the first transmitted fragment.)

    private final int maxFrameFragmentLength;

    private final int maxFrameLength;

    public FramedDataOutput(Transport transport, DataSourceRegistry dataSourceRegistry, int maxFrameFragmentLength, int maxFrameLength) {
        if(maxFrameFragmentLength<16) throw new IllegalArgumentException("maxFrameFragmentLength must be at least 8");
        if(maxFrameLength<32) throw new IllegalArgumentException("maxFrameLength must be at least 12");
        if(maxFrameLength<(maxFrameFragmentLength+estimatedMaxFragmentHeaderSize)) throw new IllegalArgumentException("maxFrameLength cannot be less than 11 + fragmentlength");
        if(transport==null) throw new IllegalArgumentException("Transport cannot be null");
        this.maxFrameFragmentLength = maxFrameFragmentLength;
        this.maxFrameLength = maxFrameLength;
        this.dataSourceIdAllocator = new IdAllocator();
        this.transport = transport;
        this.dataSourceRegistry = dataSourceRegistry;
        startSending();
    }

    public void addDataSource(DataSource dataSource){
        int id = dataSourceIdAllocator.allocate();
        dataSources.add(new DataSourceInfo(id, dataSource));
    }

    private void removeDataSource(DataSourceInfo dataSourceInfo){
        dataSources.remove(dataSourceInfo);
        dataSourceIdAllocator.release(dataSourceInfo.getId());
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
                DataSourceInfo dataSourceInfo = dataSources.pollFirst();
                DataSource dataSource = dataSourceInfo.getDataSource();
                int maxSize = Math.min(maxFrameFragmentLength, remainingSize - estimatedMaxFragmentHeaderSize);
                if(maxSize > 0 && dataSource.availableBytes()>0){
                    int dataId = dataSourceInfo.getId();
                    int chunkSizeRemaining = Math.min(dataSource.availableBytes(), maxSize);
                    ByteBuffer chunk = ByteBuffer.allocateDirect(chunkSizeRemaining);

                    // the first bytes of the transmition are reserved for the dataSourceId.
                    // currently the dataSourceId has to be fully in the first fragment.
                    //TODO: fix bug where a datasource is allways split up over at elast two fragments.
                    if(!dataSourceInfo.isStartedSending()){
                        int dataSourceId = dataSourceRegistry.getDataSourceId(dataSource);
                        VarInt.writeVarInt(chunk, dataSourceId);
                        chunkSizeRemaining -= VarInt.sizeOf(dataSourceId);
                    }

                    dataSource.read(chunk, chunkSizeRemaining);
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
