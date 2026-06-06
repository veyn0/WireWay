//package xyz.wireway.frame.datasource.debug;
//
//import xyz.wireway.Main;
//import xyz.wireway.frame.datasource.DataSource;
//import xyz.wireway.frame.datasource.DataSourceId;
//import xyz.wireway.frame.datasource.Provider;
//import xyz.wireway.util.ComposedBuffer;
//
//import java.nio.ByteBuffer;
//
//@DataSourceId(1)
//public class DebugByteArrayDatasource implements DataSource {
//
//    private ComposedBuffer buffer = new ComposedBuffer();
//
//    public DebugByteArrayDatasource(){
//
//    }
//
//    public DebugByteArrayDatasource(byte[] data) {
//        buffer.add(ByteBuffer.wrap(data));
//    }
//
//    @Override
//    public int availableBytes() {
//        return buffer.remaining();
//    }
//
//    @Override
//    public void read(ByteBuffer buffer, int length) {
//        buffer.put(this.buffer.get(length));
//    }
//
//    @Override
//    public boolean isExhausted() {
//        return buffer.remaining()<=0;
//    }
//
//
//    @Override
//    public void close() {
//        Main.printByteBufferNoFlip(buffer.get());
//    }
//
//    @Override
//    public void write(ByteBuffer buffer) {
//        this.buffer.add(buffer);
//    }
//
//    @Override
//    public void inject(Provider provider) {
//
//    }
//
//}
