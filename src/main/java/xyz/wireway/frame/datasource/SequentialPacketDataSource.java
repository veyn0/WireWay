package xyz.wireway.frame.datasource;

import xyz.wireway.protocol.Packet;
import xyz.wireway.util.ProtocolUtils;

import java.nio.ByteBuffer;

@DataSourceId(datasourceId = 3)
public class SequentialPacketDataSource extends ComposedBufferBase implements DataSource{


    public SequentialPacketDataSource() {


    }


    public void addPacket(Packet p){
        ByteBuffer buffer = p.encode();


        composedBuffer.add(Packet.getData(p, provider.getPacketRegistry()));
    }

    @Override
    protected void postWrite() {
        while (ProtocolUtils.canRead(composedBuffer)){
            Packet p = Packet.read(composedBuffer, provider.getPacketRegistry());
            provider.onPacketReceive(p);
        }
    }

    @Override
    protected void postClose() {

    }

    @Override
    public boolean isExhausted() {
        return false;
    }
}
