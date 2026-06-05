package xyz.wireway.frame.datasource;

import xyz.wireway.protocol.Packet;
import xyz.wireway.util.ProtocolUtils;

@DataSourceId(datasourceId = 3)
public class SequentialPacketDataSource extends ComposedBufferBase implements DataSource{


    public SequentialPacketDataSource() {


    }

    @Override
    protected void postWrite() {
        while (ProtocolUtils.canRead(composedBuffer)){
            Packet p = Packet.read(composedBuffer, provider.getPacketRegistry());
        }
    }

    @Override
    protected void postClose() {

    }
}
