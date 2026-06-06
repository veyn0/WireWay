package xyz.wireway.frame.channel;

import java.nio.ByteBuffer;

public interface Channel<C> {

    int availableBytes();

    void read(ByteBuffer buffer, int length);

    boolean isExhausted();

    public void close();

    void write(ByteBuffer buffer);

    void inject(C context);

    void setSubId(int subId);

    int getSubId();
}
