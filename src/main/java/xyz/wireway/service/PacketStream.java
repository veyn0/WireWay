package xyz.wireway.service;

import xyz.wireway.protocol.Packet;

import java.util.function.Consumer;

/** Request/response view onto one endpoint of the sequential packet channel. */
public final class PacketStream {

    private final PacketStreamController controller;
    private final int endpointId;

    PacketStream(PacketStreamController controller, int endpointId) {
        this.controller = controller;
        this.endpointId = endpointId;
    }

    public void send(Packet packet) {
        controller.send(endpointId, packet);
    }

    public void send(Packet packet, Consumer<Packet> onResponse) {
        send(packet, onResponse, PacketStreamController.DEFAULT_RESPONSE_TIMEOUT_MILLIS);
    }

    /** Sends a request; the callback is dropped without notice if no response arrives in time. */
    public void send(Packet packet, Consumer<Packet> onResponse, long timeoutMillis) {
        controller.send(endpointId, packet, onResponse, timeoutMillis);
    }

    public void setListener(PacketListener listener) {
        controller.setListener(endpointId, listener);
    }
}
