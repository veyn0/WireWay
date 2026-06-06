package xyz.wireway.frame.channel;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class ChannelRegistry {

    private final Map<Integer, Supplier<Channel<?>>> incomingChannelRegistry = new ConcurrentHashMap<>();
    private final Map<Class<? extends Channel<?>>, Integer> channelIdsByClass = new ConcurrentHashMap<>();
    private final Map<Integer, Object> contextsById = new ConcurrentHashMap<>();

    public ChannelRegistry(ChannelSet... sets) {
        Map<Integer, ChannelSet.ChannelEntry> registry = buildRegistry(sets);
        for (int id : registry.keySet()) {
            ChannelSet.ChannelEntry entry = registry.get(id);
            Class<? extends Channel<?>> type = entry.type();

            incomingChannelRegistry.put(id, () -> {
                try {
                    return type.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            channelIdsByClass.put(type, id);
            contextsById.put(id, entry.context());
        }
    }

    public int getChannelId(Channel<?> channel) {
        return channelIdsByClass.get(channel.getClass());
    }

    @SuppressWarnings("unchecked")
    public Channel<?> createChannel(int id) {
        Supplier<Channel<?>> supplier = incomingChannelRegistry.get(id);
        if (supplier == null) throw new IllegalArgumentException("Channel id " + id + " not registered");

        Channel<Object> channel = (Channel<Object>) supplier.get();
        channel.inject(contextsById.get(id));
        return channel;
    }

    private static Map<Integer, ChannelSet.ChannelEntry> buildRegistry(ChannelSet... sets) {
        Map<Long, ChannelSet.ChannelEntry> combined = new HashMap<>();
        for (ChannelSet set : sets) {
            combined.putAll(set.getNamedChannelRegistry());
        }
        List<Long> sortedHashes = new ArrayList<>(combined.keySet());
        Collections.sort(sortedHashes);
        Map<Integer, ChannelSet.ChannelEntry> result = new LinkedHashMap<>();
        for (int i = 0; i < sortedHashes.size(); i++) {
            result.put(i, combined.get(sortedHashes.get(i)));
        }
        return result;
    }
}