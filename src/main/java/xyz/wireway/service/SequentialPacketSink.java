package xyz.wireway.service;

import xyz.wireway.channel.DataSink;
import xyz.wireway.protocol.Packet;
import xyz.wireway.protocol.PacketRegistry;
import xyz.wireway.util.ComposedBuffer;
import xyz.wireway.wire.LengthPrefixed;
import xyz.wireway.wire.ProtocolException;
import xyz.wireway.wire.VarInt;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

/**
 * Inbound side of a packet stream. Parses records of the form
 * {@code [request : byte][referenceId : VarInt][packet record]}.
 */
final class SequentialPacketSink implements DataSink {

    private final ComposedBuffer buffer = new ComposedBuffer();
    private final PacketRegistry packets;
    private final int endpointId;
    private final Consumer<PacketInfo> out;

    SequentialPacketSink(PacketRegistry packets, int endpointId, Consumer<PacketInfo> out) {
        this.packets = packets;
        this.endpointId = endpointId;
        this.out = out;
    }

    @Override
    public void write(ByteBuffer data) {
        buffer.add(data);
        while (readRecord()) {
        }
    }

    private boolean readRecord() {
        ByteBuffer view = buffer.peek(buffer.remaining());
        boolean request;
        int referenceId;
        int length;
        try {
            request = (view.get() & 0x01) == 0x01;
            referenceId = VarInt.readVarIntSafe(view);
            length = VarInt.readVarIntSafe(view);
        } catch (BufferUnderflowException e) {
            return false;
        } catch (IllegalStateException e) {
            throw new ProtocolException("malformed packet stream record", e);
        }
        if (length < 0 || length > LengthPrefixed.MAX_RECORD_BYTES) {
            throw new ProtocolException("packet record length out of bounds: " + length);
        }
        if (view.remaining() < length) return false;
        buffer.get(1 + VarInt.sizeOf(referenceId));
        Packet packet = Packet.readRecord(buffer, packets);
        out.accept(new PacketInfo(request, referenceId, endpointId, packet));
        return true;
    }

    @Override
    public void close() {}
}
