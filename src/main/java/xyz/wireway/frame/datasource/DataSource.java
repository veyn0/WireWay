package xyz.wireway.frame.datasource;

import xyz.wireway.service.DataController;
import xyz.wireway.service.WireWay;

import java.nio.ByteBuffer;

public interface DataSource {

    int availableBytes();

    void read(ByteBuffer buffer, int length);

    boolean isExhausted();

    public void close();

    void write(ByteBuffer buffer);

    void inject(DataController dataController);

}
