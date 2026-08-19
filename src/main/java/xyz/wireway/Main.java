package xyz.wireway;

import xyz.wireway.protocol.PacketRegistry;
import xyz.wireway.protocol.Protocol;
import xyz.wireway.protocol.packet.HeartBeatPacket;
import xyz.wireway.service.PacketStream;
import xyz.wireway.service.WireWay;
import xyz.wireway.transport.adapter.SocketTransport;

/** Manual smoke test; not part of the library API. */
public class Main {

    public static void main(String[] args) throws Exception {
        Protocol protocol = new Protocol();
        protocol.register(HeartBeatPacket.class);
        PacketRegistry packets = new PacketRegistry(protocol);

        new Thread(() -> SocketTransport.listen(26656, transport -> {
            WireWay wireWay = new WireWay(transport, packets, 64, 32);
            wireWay.openStream(1).setListener(request -> {
                System.out.println("server: request received, answering");
                return new HeartBeatPacket();
            });
            wireWay.addPacketListener(p -> System.out.println("server: async packet received"));
        }), "server").start();

        Thread.sleep(500);

        WireWay client = new WireWay(SocketTransport.connect("localhost", 26656), packets, 64, 32);
        PacketStream stream = client.openStream(1);
        stream.send(new HeartBeatPacket(), response -> System.out.println("client: response received"));
        client.sendPacket(new HeartBeatPacket());
    }
}
