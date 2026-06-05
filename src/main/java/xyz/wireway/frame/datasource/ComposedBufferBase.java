package xyz.wireway.frame.datasource;

import xyz.wireway.service.DataController;
import xyz.wireway.util.ComposedBuffer;

import java.nio.ByteBuffer;

public abstract class ComposedBufferBase {

    protected boolean open = true;

    protected DataController dataController;

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

    public void inject(DataController dataController){
        this.dataController = dataController;
    }

    protected abstract void postClose();

    protected void postRead(){

    };

    protected void postWrite(){

    }

}
