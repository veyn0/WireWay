package xyz.wireway.frame.datasource;

import xyz.wireway.util.ComposedBuffer;

import java.nio.ByteBuffer;

public abstract class ComposedBufferBase {

    protected boolean open = true;

    protected Provider provider;

    protected ComposedBuffer composedBuffer = new ComposedBuffer();

    public void read(ByteBuffer buffer, int length) {
        System.out.println("reading " + length + " bytes");
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

    public void inject(Provider dataController){
        this.provider = dataController;
    }

    protected abstract void postClose();

    protected void postRead(){
        System.out.println("postRead " + availableBytes());
    };

    protected void postWrite(){

    }

}
