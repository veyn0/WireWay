package xyz.wireway.frame.transmit;

import xyz.wireway.frame.datasource.DataSourceRegistry;
import xyz.wireway.frame.Frame;
import xyz.wireway.frame.FrameFragment;
import xyz.wireway.frame.datasource.DataSource;
import xyz.wireway.transport.TransportListener;
import xyz.wireway.util.ComposedBuffer;
import xyz.wireway.util.ProtocolUtils;
import xyz.wireway.util.VarInt;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FramedDataInput implements TransportListener {

    private ComposedBuffer composedBuffer = new ComposedBuffer();

    private final DataSourceRegistry dataSourceRegistry;

    private Map<Integer, DataSource> dataSourcesByDataId = new ConcurrentHashMap<>();

    public FramedDataInput(DataSourceRegistry dataSourceRegistry) {
        this.dataSourceRegistry = dataSourceRegistry;
    }

    @Override
    public void onReceive(ByteBuffer data) {
        composedBuffer.add(data);
        handleIncomingFrames();
    }

    private void handleIncomingFrames(){
        while (ProtocolUtils.canRead( composedBuffer)){
            System.out.println("reading Frame");

            Frame frame = Frame.read(composedBuffer);
            for(FrameFragment fragment : frame.getFragments()){
                handleFrameFragment(fragment);
            }
        }
    }

    private void handleFrameFragment(FrameFragment f){
        int dataId = f.getDataId();
        ByteBuffer data = f.getData();
        if(f.isStart()){
            // the first bytes of a datasource always has to be the id their class is registered under DataSourceRegistry.
            // this process should be handled on another layer or reworked to avoid the additional edge cases it introduces.
            int dataSourceId = VarInt.readVarInt(data);
            dataSourcesByDataId.put(dataId, dataSourceRegistry.createDataSource(dataSourceId));
        }
        DataSource dataSource = dataSourcesByDataId.get(dataId);
        dataSource.write(f.getData());
        if(f.isEnd()) dataSource.close();
    }

}
