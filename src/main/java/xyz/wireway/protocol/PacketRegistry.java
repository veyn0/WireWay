package xyz.wireway.protocol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class PacketRegistry {

    private final Map<Integer, Supplier<Packet>> incomingPacketRegistry = new ConcurrentHashMap<>();
    private final Map<Class<? extends Packet>, Integer> packetIdsByClass = new ConcurrentHashMap<>();

    public PacketRegistry(Protocol... protocols) {
        Map<Integer, Class<? extends Packet>> packetRegistry = buildRegistry(protocols);
        for (Map.Entry<Integer, Class<? extends Packet>> entry : packetRegistry.entrySet()) {
            Class<? extends Packet> type = entry.getValue();
            incomingPacketRegistry.put(entry.getKey(), () -> {
                try {
                    return type.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            packetIdsByClass.put(type, entry.getKey());
        }
    }

    public int getPacketId(Packet packet) {
        Integer id = packetIdsByClass.get(packet.getClass());
        if (id == null) {
            throw new IllegalArgumentException("packet class not registered: " + packet.getClass().getName());
        }
        return id;
    }

    public Packet createPacket(int id) {
        Supplier<Packet> supplier = incomingPacketRegistry.get(id);
        if (supplier == null) {
            throw new IllegalArgumentException(String.format("Packet with id %d not registered", id));
        }
        return supplier.get();
    }

    private static Map<Integer, Class<? extends Packet>> buildRegistry(Protocol... protocols) {
        Map<Long, Class<? extends Packet>> combined = new HashMap<>();
        for (Protocol protocol : protocols) {
            for (Map.Entry<Long, Class<? extends Packet>> entry : protocol.getNamedPacketRegistry().entrySet()) {
                Class<? extends Packet> previous = combined.putIfAbsent(entry.getKey(), entry.getValue());
                if (previous != null && previous != entry.getValue()) {
                    throw new IllegalArgumentException("packet name hash collision: "
                            + entry.getValue().getName() + " / " + previous.getName());
                }
            }
        }
        List<Long> sortedHashes = new ArrayList<>(combined.keySet());
        Collections.sort(sortedHashes);
        Map<Integer, Class<? extends Packet>> result = new HashMap<>();
        for (int i = 0; i < sortedHashes.size(); i++) {
            result.put(i, combined.get(sortedHashes.get(i)));
        }
        return result;
    }
}
