package xyz.wireway.protocol;

import xyz.wireway.util.ComposedBuffer;
import xyz.wireway.wire.LengthPrefixed;
import xyz.wireway.wire.VarInt;

import java.nio.ByteBuffer;

public interface Packet {

    void decode(ByteBuffer buffer);

    ByteBuffer encode();

    /** Encodes one record: {@code [length : VarInt][packetId : VarInt][body]}. */
    static ByteBuffer encodeRecord(Packet packet, PacketRegistry registry) {
        ByteBuffer body = packet.encode();
        int packetId = registry.getPacketId(packet);
        int payloadLength = VarInt.sizeOf(packetId) + body.remaining();
        ByteBuffer record = ByteBuffer.allocate(LengthPrefixed.recordSize(payloadLength));
        VarInt.writeVarInt(record, payloadLength);
        VarInt.writeVarInt(record, packetId);
        record.put(body);
        return record.flip();
    }

    /** Consumes one complete record. Call only after {@link LengthPrefixed#hasRecord}. */
    static Packet readRecord(ComposedBuffer buffer, PacketRegistry registry) {
        ByteBuffer payload = LengthPrefixed.takeRecord(buffer);
        int packetId = VarInt.readVarInt(payload);
        Packet packet = registry.createPacket(packetId);
        packet.decode(payload);
        return packet;
    }
}
