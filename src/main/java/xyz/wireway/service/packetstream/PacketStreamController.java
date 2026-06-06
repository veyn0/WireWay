package xyz.wireway.service.packetstream;

import xyz.wireway.frame.channel.ChannelRegistry;
import xyz.wireway.frame.channel.impl.SequentialPacketChannel;
import xyz.wireway.frame.transmit.FramedDataOutput;
import xyz.wireway.frame.transmit.OutgoingChannelWrapper;
import xyz.wireway.protocol.Packet;
import xyz.wireway.protocol.PacketRegistry;
import xyz.wireway.util.IdAllocator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class PacketStreamController {

    private final Map<Integer, PacketListener> listenerBySubId = new ConcurrentHashMap<>();

    private final Map<Integer, SequentialPacketChannel> channelsBySubId = new ConcurrentHashMap<>();

    private final Map<Integer, Map<Integer, Consumer<Packet>>> responseConsumerByReferenceIdBySubId = new ConcurrentHashMap<>();

    private final PacketRegistry packetRegistry;

    private final IdAllocator packetReferenceIdAllocator = new IdAllocator();

    private final FramedDataOutput framedDataOutput;

    private final ChannelRegistry channelRegistry;

    private final PacketChannelContext packetChannelContext;

    public PacketStreamController(PacketRegistry packetRegistry, FramedDataOutput framedDataOutput, ChannelRegistry channelRegistry, PacketChannelContext packetChannelContext) {
        this.framedDataOutput = framedDataOutput;
        this.packetRegistry = packetRegistry;
        this.channelRegistry = channelRegistry;
        this.packetChannelContext = packetChannelContext;
        packetReferenceIdAllocator.allocate();
    }

    public void onPacketReceive(PacketInfo packetInfo){
        if(packetInfo.isRequest()) {
            PacketListener l = listenerBySubId.get(packetInfo.getSubId());
            Packet p = l.onPacketReceive(packetInfo.getP());
            if (p == null) return;

            channelsBySubId.get(packetInfo.getSubId()).addPacket(p, false, packetInfo.getReferenceId());

        }else {
            Consumer<Packet> packetConsumer = responseConsumerByReferenceIdBySubId.get(packetInfo.getSubId()).get(packetInfo.getReferenceId());
            if(packetConsumer == null) return;
            packetConsumer.accept(packetInfo.getP());

        }
    }


    public PacketStream createPacketChannel(int id){
        SequentialPacketChannel channel = new SequentialPacketChannel();
        channel.inject(packetChannelContext);
        channel.setSubId(id);

        channelsBySubId.put(id, channel);

        framedDataOutput.addChannel(new OutgoingChannelWrapper(channel, channelRegistry));

        return new PacketStream(
                (packet, onSendPacket) ->{
                    if(onSendPacket==null){
                        channel.addPacket(packet, true, 0);
                    }
                    else{
                        int referenceId = packetReferenceIdAllocator.allocate();
                        channel.addPacket(packet, true, referenceId);
                        if(!responseConsumerByReferenceIdBySubId.containsKey(id)) responseConsumerByReferenceIdBySubId.put(id, new ConcurrentHashMap<>());
                        responseConsumerByReferenceIdBySubId.get(id).put(referenceId, onSendPacket);
                    }
                },
                packetListener -> {
                    setListener(id, packetListener);
                }
        );
    }

    private void setListener(int id, PacketListener listener){
        listenerBySubId.put(id, listener);
    }
}
