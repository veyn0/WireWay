package xyz.wireway.protocol;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class PacketRegistry {

    private final Map<Integer, Supplier<Packet>> incomingPacketRegistry = new ConcurrentHashMap<>();

    private final Map<Class<? extends Packet>, Integer> packetIdsByClass = new ConcurrentHashMap<>();

    public PacketRegistry(Protocol... protocol) {
        Map<Integer, Class<? extends Packet>> packetRegistry = buildRegistry(protocol);
        for(int i : packetRegistry.keySet()){
            Class<? extends Packet> currentPacket = packetRegistry.get(i);
            incomingPacketRegistry.put(i, () -> {
               try {
                    return currentPacket.getDeclaredConstructor().newInstance();
               } catch (Exception e){
                   throw new RuntimeException(e);
               }
            });
            packetIdsByClass.put(packetRegistry.get(i), i);
        }
    }

    public int getPacketId(Packet p){
        return packetIdsByClass.get(p.getClass());
    }

    public Packet createPacket(int id){
        Supplier<Packet> packetSupplier = incomingPacketRegistry.get(id);
        if(packetSupplier==null) throw new IllegalArgumentException(String.format("Packet with id %d not registered", id));
        return packetSupplier.get();
    }

    private static Map<Integer, Class<? extends Packet>> buildRegistry(Protocol... protocols) {
        Map<Long, Class<? extends Packet>> combined = new HashMap<>();
        for (Protocol p : protocols) {
            combined.putAll(p.getNamedPacketRegistry());
        }
        List<Long> sortedHashes = new ArrayList<>(combined.keySet());
        Collections.sort(sortedHashes);
        Map<Integer, Class<? extends Packet>> result = new LinkedHashMap<>();
        for (int i = 0; i < sortedHashes.size(); i++) {
            result.put(i, combined.get(sortedHashes.get(i)));
        }
        return result;
    }

}
