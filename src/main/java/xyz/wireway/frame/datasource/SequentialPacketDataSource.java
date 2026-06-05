package xyz.wireway.frame.datasource;

@DataSourceId(datasourceId = 3)
public class SequentialPacketDataSource extends ComposedBufferBase implements DataSource{


    public SequentialPacketDataSource() {
    }

    @Override
    protected void postWrite() {
        super.postWrite();

    }

    @Override
    protected void postClose() {

    }
}
