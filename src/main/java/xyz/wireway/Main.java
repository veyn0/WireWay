package xyz.wireway;

import xyz.wireway.protocol.Packet;
import xyz.wireway.protocol.PacketRegistry;
import xyz.wireway.protocol.Protocol;
import xyz.wireway.protocol.packet.HeartBeatPacket;
import xyz.wireway.service.PacketChannel;
import xyz.wireway.service.WireWay;
import xyz.wireway.service.packetstream.PacketListener;
import xyz.wireway.service.packetstream.PacketStream;
import xyz.wireway.transport.Transport;
import xyz.wireway.transport.adapter.SocketTransport;

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
            try {
                createClient();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();




    }

    private static void createClient() throws Exception{
        Transport t = SocketTransport.connect("localhost", 26656);
        Protocol p = new Protocol();
        p.register(HeartBeatPacket.class);
        PacketRegistry pr = new PacketRegistry(p);



        WireWay wireWay = new WireWay(t, pr, 64, 32);

        PacketChannel pc = wireWay.createPacketChannel(1);
        pc.sendPacket(new HeartBeatPacket(), packet -> {
            System.out.println("test");
        });

    }

    private static void startListening(){
        Protocol p = new Protocol();
        p.register(HeartBeatPacket.class);
        PacketRegistry pr = new PacketRegistry(p);

        SocketTransport.listen(
                26656,
                transport -> {
                    WireWay wireWay = new WireWay(transport, pr, 64, 32);

                    PacketChannel pc = wireWay.createPacketChannel(1);
                    pc.setListener(createListener(1));

                }
        );

    }


    private static PacketListener createListener(int id){
        return new PacketListener() {
            @Override
            public Packet onPacketReceive(Packet p) {
                System.out.println("Packet received on ID " + id);
                return new HeartBeatPacket();
            }
        };
    }






    private void test1(){
//        Transport t = LoopbackTransport.connect();
//        ChannelRegistry channelRegistry = new ChannelRegistry()
//
////        dataSourceRegistry.registerDataSource(DebugByteArrayDatasource.class);
////        dataSourceRegistry.registerDataSource(DebugStringDataSource.class);
//
//        t.addListener(new DebugtransportListener());
//        t.addListener(new FramedDataInput(dataSourceRegistry));
//        FramedDataOutput fdOut = new FramedDataOutput(t, dataSourceRegistry ,1024, 10240);
//        byte[] data = {125, 126, 127, 1,1,1,1, 2,1,1,1, 3,1,1,1, 4,1,1,1, 5,1,1,1, 6,1,1,1, 7,1,1,1, 8,1,1,1, 9,1,1,1, 10,1,1,1};
////        Channel channel = new DebugByteArrayDatasource(data.clone());
////        Channel channel1 = new DebugByteArrayDatasource(data.clone());
////        Channel channel2 = new DebugByteArrayDatasource(data.clone());
////        Channel channel3 = new DebugByteArrayDatasource(data.clone());
////        Channel channel4 = new DebugByteArrayDatasource(data.clone());
////        Channel text = new DebugStringDataSource("meddl leude was geht");
//        fdOut.addDataSource(channel);
//        fdOut.addDataSource(channel1);
//        fdOut.addDataSource(channel2);
//        fdOut.addDataSource(channel3);
//        fdOut.addDataSource(channel4);
//        fdOut.addDataSource(text);
//
//        fdOut.sendFrame();
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