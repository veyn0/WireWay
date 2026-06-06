package xyz.wireway.frame.channel;

import xyz.wireway.util.ComposedBuffer;

import java.nio.ByteBuffer;

public abstract class ComposedBufferBase {

    protected int subId;

    protected boolean open = true;

    protected ComposedBuffer composedBuffer = new ComposedBuffer();

    public void read(ByteBuffer buffer, int length) {
        buffer.put(this.composedBuffer.get(length));
        postRead();
    }

    public void write(ByteBuffer buffer) {
        this.composedBuffer.add(buffer);
        postWrite();
    }

    public boolean isExhausted() {
        return composedBuffer.remaining()<=0;
    }

    public int availableBytes(){
        return composedBuffer.remaining();
    }

    public void close(){
        open = false;
        postClose();
    }

    protected abstract void postClose();

    protected void postRead(){

    };

    protected void postWrite(){

    }


    public void setSubId(int subId) {
        this.subId = subId;
    }

    public int getSubId() {

        return subId;
    }
}
