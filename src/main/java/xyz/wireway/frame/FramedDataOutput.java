package xyz.wireway.frame;

import xyz.wireway.transport.Transport;
import xyz.wireway.util.IdAllocator;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FramedDataOutput {

    //private Map<Integer, DataSource> dataSources = new ConcurrentHashMap<>();

    private final Deque<DataSourceInfo> dataSources = new ArrayDeque<>();

    private int startIndex = 0;

    private final IdAllocator dataSourceIdAllocator;

    private Transport transport;

    private static final int estimatedMaxFragmentHeaderSize = 11; // 5 + 1 + 5

    private final int maxFrameFragmentLength;

    private final int maxFrameLength;

    public FramedDataOutput(int maxFrameFragmentLength, int maxFrameLength) {
        if(maxFrameFragmentLength<32) throw new IllegalArgumentException("maxFrameFragmentLength must be at least 32");
        if(maxFrameLength<64) throw new IllegalArgumentException("maxFrameLength must be at least 64");
        this.maxFrameFragmentLength = maxFrameFragmentLength;
        this.maxFrameLength = maxFrameLength;
        this.dataSourceIdAllocator = new IdAllocator();
    }

    public void addDataSource(DataSource dataSource){
        int id = dataSourceIdAllocator.allocate();
        dataSources.add(new DataSourceInfo(id, dataSource));
    }

    private void removeDataSource(DataSourceInfo dataSourceInfo){
        dataSources.remove(dataSourceInfo);
        dataSourceIdAllocator.release(dataSourceInfo.getId());
    }

    private void outputLoop(){


    }

    private Frame buildNextFrame(){
        List<FrameFragment> fragments = new ArrayList<>();
        int remainingSize = maxFrameLength;
        boolean progressed = true;

        while (progressed && remainingSize > 0 && !fragments.isEmpty()){
            progressed = false;
            int passSize = dataSources.size();

            for(int i = 0; i < passSize; i++){
                DataSourceInfo dataSourceInfo = dataSources.pollFirst();
                DataSource dataSource = dataSourceInfo.getDataSource();

                int maxSize = Math.min(maxFrameFragmentLength, remainingSize - estimatedMaxFragmentHeaderSize);

                if(maxSize > 0 && dataSource.availableBytes()>0){
                    int chunkSize = Math.min(dataSource.availableBytes(), maxSize);
                    ByteBuffer chunk = ByteBuffer.allocateDirect(chunkSize);


                    dataSource.write(chunk, chunkSize);



                }


            }



        }

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
