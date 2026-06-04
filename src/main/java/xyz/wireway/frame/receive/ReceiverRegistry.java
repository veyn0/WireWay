package xyz.wireway.frame.receive;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class ReceiverRegistry {

    private final Map<Integer, Supplier<DataReceiver>> factories = new ConcurrentHashMap<>();

    public void register(int typeId, Supplier<DataReceiver> factory) {
        if (factories.putIfAbsent(typeId, factory) != null)
            throw new IllegalArgumentException("typeId already registered: " + typeId);
    }

    public DataReceiver create(int typeId) {
        Supplier<DataReceiver> f = factories.get(typeId);
        if (f == null) throw new IllegalArgumentException("Unknown stream type id: " + typeId);
        return f.get();
    }

}
