package xyz.wireway.protocol;

import xyz.wireway.util.ProtocolUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Protocol {

    private final Map<Long, Class<? extends Packet>> namedPacketRegistry = new ConcurrentHashMap<>();

    public void register(Class<? extends Packet> packet){
        PacketId id = packet.getAnnotation(PacketId.class);
        if(id == null) throw new IllegalArgumentException("Packet class must annotate @PacketId");
        long idHash = ProtocolUtils.packetIdHash(id.value());
        namedPacketRegistry.put(idHash, packet);
    }

    public Map<Long, Class<? extends Packet>> getNamedPacketRegistry() {
        return namedPacketRegistry;
    }

}
