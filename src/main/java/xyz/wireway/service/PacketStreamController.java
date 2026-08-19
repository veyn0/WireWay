package xyz.wireway.service;

import xyz.wireway.channel.DataSink;
import xyz.wireway.protocol.Packet;
import xyz.wireway.protocol.PacketRegistry;
import xyz.wireway.util.IdAllocator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.IntFunction;

/**
 * Request/response bookkeeping for packet streams. All inbound dispatch runs on
 * the dispatcher thread; sends may come from any thread.
 */
final class PacketStreamController {

    static final long DEFAULT_RESPONSE_TIMEOUT_MILLIS = 30_000;

    private record Pending(Consumer<Packet> onResponse, ScheduledFuture<?> timeout) {}

    private final PacketRegistry packets;
    private final ScheduledExecutorService dispatcher;
    private final IntFunction<PacketOutbox> outboxOpener;
    private final IdAllocator referenceIds = new IdAllocator();
    private final Map<Integer, PacketOutbox> outboxByEndpoint = new ConcurrentHashMap<>();
    private final Map<Integer, PacketListener> listenerByEndpoint = new ConcurrentHashMap<>();
    private final Map<Long, Pending> pending = new ConcurrentHashMap<>();

    PacketStreamController(PacketRegistry packets, ScheduledExecutorService dispatcher,
                           IntFunction<PacketOutbox> outboxOpener) {
        this.packets = packets;
        this.dispatcher = dispatcher;
        this.outboxOpener = outboxOpener;
        referenceIds.allocate(); // reference id 0 means "no response expected"
    }

    DataSink createSink(int endpointId) {
        return new SequentialPacketSink(packets, endpointId,
                info -> dispatcher.execute(() -> dispatch(info)));
    }

    void setListener(int endpointId, PacketListener listener) {
        listenerByEndpoint.put(endpointId, listener);
    }

    void send(int endpointId, Packet packet) {
        outbox(endpointId).add(true, 0, Packet.encodeRecord(packet, packets));
    }

    void send(int endpointId, Packet packet, Consumer<Packet> onResponse, long timeoutMillis) {
        int referenceId = referenceIds.allocate();
        long key = key(endpointId, referenceId);
        ScheduledFuture<?> timeout =
                dispatcher.schedule(() -> abandon(key, referenceId), timeoutMillis, TimeUnit.MILLISECONDS);
        pending.put(key, new Pending(onResponse, timeout));
        outbox(endpointId).add(true, referenceId, Packet.encodeRecord(packet, packets));
    }

    private void dispatch(PacketInfo info) {
        if (info.request()) {
            PacketListener listener = listenerByEndpoint.get(info.endpointId());
            if (listener == null) return;
            Packet response = listener.onPacketReceive(info.packet());
            if (response != null && info.referenceId() != 0) {
                outbox(info.endpointId()).add(false, info.referenceId(), Packet.encodeRecord(response, packets));
            }
        } else {
            Pending request = pending.remove(key(info.endpointId(), info.referenceId()));
            if (request == null) return;
            request.timeout().cancel(false);
            referenceIds.release(info.referenceId());
            request.onResponse().accept(info.packet());
        }
    }

    private void abandon(long key, int referenceId) {
        if (pending.remove(key) != null) {
            referenceIds.release(referenceId);
        }
    }

    private PacketOutbox outbox(int endpointId) {
        return outboxByEndpoint.computeIfAbsent(endpointId, outboxOpener::apply);
    }

    void shutdown() {
        for (Pending request : pending.values()) {
            request.timeout().cancel(false);
        }
        pending.clear();
    }

    private static long key(int endpointId, int referenceId) {
        return ((long) endpointId << 32) | (referenceId & 0xFFFFFFFFL);
    }
}
