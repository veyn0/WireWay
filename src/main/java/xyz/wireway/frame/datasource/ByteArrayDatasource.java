package xyz.wireway.frame.datasource;

import java.nio.ByteBuffer;

public class ByteArrayDatasource implements DataSource{


    private int index = 0;
    private final byte[] data;

    public ByteArrayDatasource(byte[] data) {
        this.data = data;
    }

    @Override
    public int availableBytes() {
        return data.length - index;
    }

    @Override
    public void write(ByteBuffer buffer, int length) {
        buffer.put(data, index, length);
        index += length;
    }

    @Override
    public boolean isExhausted() {
        return data.length <= index;
    }
}
