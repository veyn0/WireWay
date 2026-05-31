package xyz.wireway.protocol;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class PacketRegistry {

    private final Map<Integer, Supplier<Packet>> incomingPacketRegistry = new ConcurrentHashMap<>();

    private final Map<Class<? extends Packet>, Integer> packetIdsByClass = new ConcurrentHashMap<>();

    public PacketRegistry(Map<Integer, Class<? extends Packet>> packetRegistry) {
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

}
