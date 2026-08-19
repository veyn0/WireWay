package xyz.wireway.channel;

import xyz.wireway.util.NameHash;
import xyz.wireway.wire.ProtocolException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Assigns numeric channel type ids by sorting the name hashes of all registered
 * channels, so both peers derive the same mapping from the same set of names.
 */
public final class ChannelRegistry {

    private final Map<String, Integer> idsByName = new HashMap<>();
    private final SinkFactory[] factoriesById;

    public ChannelRegistry(ChannelSet... sets) {
        Map<Long, String> namesByHash = new HashMap<>();
        Map<String, SinkFactory> factoriesByName = new HashMap<>();
        for (ChannelSet set : sets) {
            for (Map.Entry<String, SinkFactory> entry : set.snapshot().entrySet()) {
                long hash = NameHash.hash(entry.getKey());
                String previous = namesByHash.putIfAbsent(hash, entry.getKey());
                if (previous != null) {
                    throw new IllegalArgumentException(
                            "duplicate channel name or hash collision: " + entry.getKey() + " / " + previous);
                }
                factoriesByName.put(entry.getKey(), entry.getValue());
            }
        }
        List<Long> sortedHashes = new ArrayList<>(namesByHash.keySet());
        Collections.sort(sortedHashes);
        factoriesById = new SinkFactory[sortedHashes.size()];
        for (int id = 0; id < sortedHashes.size(); id++) {
            String name = namesByHash.get(sortedHashes.get(id));
            idsByName.put(name, id);
            factoriesById[id] = factoriesByName.get(name);
        }
    }

    public int typeId(String name) {
        Integer id = idsByName.get(name);
        if (id == null) throw new IllegalArgumentException("channel not registered: " + name);
        return id;
    }

    public DataSink createSink(int typeId, int endpointId) {
        if (typeId < 0 || typeId >= factoriesById.length) {
            throw new ProtocolException("unknown channel type id: " + typeId);
        }
        return factoriesById[typeId].create(endpointId);
    }
}
