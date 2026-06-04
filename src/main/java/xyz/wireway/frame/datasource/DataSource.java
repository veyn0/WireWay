package xyz.wireway.frame.datasource;

import java.nio.ByteBuffer;

public interface DataSource {

    int availableBytes();

    void read(ByteBuffer buffer, int length);

    boolean isExhausted();

}
