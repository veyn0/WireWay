//package xyz.wireway.frame.datasource.debug;
//
//import xyz.wireway.Main;
//import xyz.wireway.frame.datasource.DataSource;
//import xyz.wireway.frame.datasource.DataSourceId;
//import xyz.wireway.frame.datasource.Provider;
//import xyz.wireway.util.ComposedBuffer;
//
//import java.nio.ByteBuffer;
//import java.nio.charset.StandardCharsets;
//
//@DataSourceId(2)
//public class DebugStringDataSource implements DataSource {
//
//    private ComposedBuffer buffer = new ComposedBuffer(1024);
//
//    // empty constructor for DataSourceRegistry functionality;
//    public DebugStringDataSource(){
//
//    }
//
//    public DebugStringDataSource(String content){
//        buffer.add(ByteBuffer.wrap(content.getBytes(StandardCharsets.UTF_8)));
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
//    @Override
//    public void close() {
//        Main.printByteBufferNoFlip(buffer.peek(buffer.remaining()));
//        System.out.println(StandardCharsets.UTF_8.decode(buffer.get()).toString());
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
