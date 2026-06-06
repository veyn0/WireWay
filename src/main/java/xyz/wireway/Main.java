package xyz.wireway;

import xyz.wireway.frame.channel.ChannelSet;
import xyz.wireway.protocol.Packet;
import xyz.wireway.protocol.PacketRegistry;
import xyz.wireway.protocol.Protocol;
import xyz.wireway.protocol.packet.HeartBeatPacket;
import xyz.wireway.service.asyncpacketstream.AsyncPacketListener;
import xyz.wireway.service.packetstream.PacketStream;
import xyz.wireway.service.WireWay;
import xyz.wireway.service.packetstream.PacketListener;
import xyz.wireway.transport.Transport;
import xyz.wireway.transport.adapter.SocketTransport;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class Main {

    //only for testing purposes lol
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



        WireWay wireWay = new WireWay(t, pr, new ChannelSet(), 64, 32);

        PacketStream pc = wireWay.createPacketChannel(1);
        pc.sendPacket(new HeartBeatPacket(), packet -> {
            System.out.println("test");
        });

        wireWay.sendPacketAsync(new HeartBeatPacket());

    }

    private static void startListening(){
        Protocol p = new Protocol();
        p.register(HeartBeatPacket.class);
        PacketRegistry pr = new PacketRegistry(p);

        SocketTransport.listen(
                26656,
                transport -> {
                    WireWay wireWay = new WireWay(transport, pr, new ChannelSet(), 64, 32);

                    PacketStream pc = wireWay.createPacketChannel(1);
                    pc.setListener(createListener(1));

                    wireWay.addAsyncPacketChannelListener(new AsyncPacketListener() {
                        @Override
                        public void onPacketReceive(Packet p) {
                            if (p instanceof HeartBeatPacket p1){
                                System.out.println("heartbeat eingetroffen");
                            }
                        }
                    });

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


    public static void printByteBufferNoFlip(ByteBuffer data){
        ByteBuffer buffer = data.duplicate();
        System.out.println("[DEBUG] ByteBuffer content:");
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        System.out.println(Arrays.toString(bytes));
    }

}