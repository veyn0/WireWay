package xyz.wireway.channel;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Named channel registrations; combined into a {@link ChannelRegistry} without being mutated. */
public final class ChannelSet {

    private final Map<String, SinkFactory> factories = new LinkedHashMap<>();

    public synchronized void register(String name, SinkFactory factory) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(factory, "factory");
        if (factories.putIfAbsent(name, factory) != null) {
            throw new IllegalArgumentException("channel already registered: " + name);
        }
    }

    synchronized Map<String, SinkFactory> snapshot() {
        return new LinkedHashMap<>(factories);
    }
}
