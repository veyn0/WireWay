package xyz.wireway.frame.channel;

import xyz.wireway.util.ProtocolUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChannelSet {

    public record ChannelEntry(Class<? extends Channel<?>> type, Object context) {}

    private final Map<Long, ChannelEntry> namedChannelRegistry = new ConcurrentHashMap<>();

    public <C> void register(Class<? extends Channel<C>> channel, C context) {
        ChannelId id = channel.getAnnotation(ChannelId.class);
        if (id == null) throw new IllegalArgumentException("Channel class must annotate @ChannelId");
        long idHash = ProtocolUtils.packetIdHash(id.value());
        if (namedChannelRegistry.containsKey(idHash))
            throw new IllegalArgumentException("Channel name hash collision for: " + id.value());
        namedChannelRegistry.put(idHash, new ChannelEntry(channel, context));
    }

    public Map<Long, ChannelEntry> getNamedChannelRegistry() {
        return namedChannelRegistry;
    }

}