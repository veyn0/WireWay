package xyz.wireway.protocol;

import xyz.wireway.util.NameHash;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Protocol {

    private final Map<Long, Class<? extends Packet>> namedPacketRegistry = new ConcurrentHashMap<>();

    public void register(Class<? extends Packet> packet) {
        PacketId id = packet.getAnnotation(PacketId.class);
        if (id == null) throw new IllegalArgumentException("Packet class must annotate @PacketId");
        long idHash = NameHash.hash(id.value());
        Class<? extends Packet> previous = namedPacketRegistry.putIfAbsent(idHash, packet);
        if (previous != null && previous != packet) {
            throw new IllegalArgumentException(
                    "packet name or hash collision: " + packet.getName() + " / " + previous.getName());
        }
    }

    public Map<Long, Class<? extends Packet>> getNamedPacketRegistry() {
        return namedPacketRegistry;
    }
}
