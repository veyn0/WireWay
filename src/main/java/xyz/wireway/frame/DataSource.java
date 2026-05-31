package xyz.wireway.frame;

import java.nio.ByteBuffer;

public interface DataSource {

    public int availableBytes();

    public void write(ByteBuffer buffer, int length);

    public boolean isExhausted();

}
