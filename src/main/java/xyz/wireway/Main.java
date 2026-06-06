package xyz.wireway;

import xyz.wireway.frame.datasource.debug.DebugByteArrayDatasource;
import xyz.wireway.frame.datasource.DataSource;
import xyz.wireway.frame.datasource.DataSourceRegistry;
import xyz.wireway.frame.datasource.debug.DebugStringDataSource;
import xyz.wireway.frame.transmit.FramedDataInput;
import xyz.wireway.frame.transmit.FramedDataOutput;
import xyz.wireway.protocol.PacketRegistry;
import xyz.wireway.protocol.Protocol;
import xyz.wireway.protocol.packet.HeartBeatPacket;
import xyz.wireway.service.WireWay;
import xyz.wireway.service.channel.SequentialPacketChannel;
import xyz.wireway.transport.Transport;
import xyz.wireway.transport.adapter.LoopbackTransport;
import xyz.wireway.transport.adapter.SocketTransport;
import xyz.wireway.transport.listener.DebugtransportListener;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class Main {


    public static void main(String[] args) throws Exception{
        new Thread(() ->{
            startListening();
        }).start();

        Thread.sleep(1000);
        System.out.println("connecting");
        new Thread(() ->{
            createTest1();
        }).start();




    }

    private static void createTest1(){
        Protocol p = new Protocol();
        p.register(HeartBeatPacket.class);
        PacketRegistry packetRegistry = new PacketRegistry(p);
        Transport t = SocketTransport.connect("localhost", 26656);

        new WireWay(t, DataSourceRegistry.createDefault(), packetRegistry, 10240, 1024);
    }

    private static void startListening(){

        Protocol p = new Protocol();
        p.register(HeartBeatPacket.class);
        PacketRegistry packetRegistry = new PacketRegistry(p);
        SocketTransport.listen(26656, transport -> {
            System.out.println("connection incoming");
            WireWay wireWay = new WireWay(transport, DataSourceRegistry.createDefault(), packetRegistry, 512, 128);
            try {
                Thread.sleep(2000);
                System.out.println("sending heartbeat");
                HeartBeatPacket packet = new HeartBeatPacket();
                SequentialPacketChannel channel = wireWay.createSequentialPacketChannel();
                channel.submitPacket(packet);
            }catch (Exception e){
                throw new RuntimeException(e);
            }
        });

    }









    private void test1(){
        Transport t = LoopbackTransport.connect();
        DataSourceRegistry dataSourceRegistry = new DataSourceRegistry();

        dataSourceRegistry.registerDataSource(DebugByteArrayDatasource.class);
        dataSourceRegistry.registerDataSource(DebugStringDataSource.class);

        t.addListener(new DebugtransportListener());
        t.addListener(new FramedDataInput(dataSourceRegistry));
        FramedDataOutput fdOut = new FramedDataOutput(t, dataSourceRegistry ,1024, 10240);
        byte[] data = {125, 126, 127, 1,1,1,1, 2,1,1,1, 3,1,1,1, 4,1,1,1, 5,1,1,1, 6,1,1,1, 7,1,1,1, 8,1,1,1, 9,1,1,1, 10,1,1,1};
        DataSource dataSource = new DebugByteArrayDatasource(data.clone());
        DataSource dataSource1 = new DebugByteArrayDatasource(data.clone());
        DataSource dataSource2 = new DebugByteArrayDatasource(data.clone());
        DataSource dataSource3 = new DebugByteArrayDatasource(data.clone());
        DataSource dataSource4 = new DebugByteArrayDatasource(data.clone());
        DataSource text = new DebugStringDataSource("meddl leude was geht");
        fdOut.addDataSource(dataSource);
        fdOut.addDataSource(dataSource1);
        fdOut.addDataSource(dataSource2);
        fdOut.addDataSource(dataSource3);
        fdOut.addDataSource(dataSource4);
        fdOut.addDataSource(text);

        fdOut.sendFrame();
    }

    public static void printByteBuffer(ByteBuffer data){

        ByteBuffer buffer = data.duplicate();
        buffer.flip();
        System.out.println("[DEBUG] ByteBuffer content:");
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        System.out.println(Arrays.toString(bytes));
    }


    public static void printByteBufferNoFlip(ByteBuffer data){
        return;
//        ByteBuffer buffer = data.duplicate();
//        System.out.println("[DEBUG] ByteBuffer content:");
//        byte[] bytes = new byte[buffer.remaining()];
//        buffer.get(bytes);
//        System.out.println(Arrays.toString(bytes));
    }

}