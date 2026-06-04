package xyz.wireway.frame.datasource;

import java.nio.ByteBuffer;

public interface DataSource {

    public int availableBytes();


    /**
    @param length has to be lower or equals to availableBytes().
     */
    public void write(ByteBuffer buffer, int length);

    public boolean isExhausted();

}
