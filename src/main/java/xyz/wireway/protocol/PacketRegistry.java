package xyz.wireway.protocol;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class PacketRegistry {

    private final Map<Integer, Supplier<Packet>> registry = new ConcurrentHashMap<>();

    public void register(Class<? extends Packet> packet){
        PacketId id = packet.getAnnotation(PacketId.class);
        if(id == null) throw new IllegalArgumentException("Packet class must annotate @PacketId");
        registry.put(id.packetId(), () ->{
            try {
            return packet.getDeclaredConstructor().newInstance();
            } catch (Exception e){
                throw new RuntimeException(e);
            }
        });

    }

    public Packet create(int id){
        Supplier<Packet> packetSupplier = registry.get(id);
        if(packetSupplier==null) throw new IllegalArgumentException(String.format("Packet with id %d not registered", id));
        return packetSupplier.get();
    }

}
