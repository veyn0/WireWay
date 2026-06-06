package xyz.wireway.protocol;

import xyz.wireway.util.ComposedBuffer;
import xyz.wireway.util.VarInt;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public interface Packet {

    void decode(ByteBuffer buffer);

    ByteBuffer encode();

    static ByteBuffer getData(Packet p, PacketRegistry packetRegistry){
        ByteBuffer content = p.encode();
        int packetId = packetRegistry.getPacketId(p);
        int length = content.remaining() + VarInt.sizeOf(packetId);
        ByteBuffer result = ByteBuffer.allocateDirect(length + VarInt.sizeOf(length));
        VarInt.writeVarInt(result, length);
        VarInt.writeVarInt(result, packetId);
        result.put(content);
        return  result.flip();
    }

    static Packet read(ComposedBuffer buffer, PacketRegistry packetRegistry){
        int len = VarInt.readVarInt(buffer.peek(5));
        return read(buffer.get(len + VarInt.sizeOf(len)), packetRegistry);
    }

    static Packet read(ByteBuffer buffer , PacketRegistry packetRegistry){
        int length = VarInt.readVarInt(buffer);
        if(buffer.remaining()<length) throw new BufferUnderflowException();
        int packetId = VarInt.readVarInt(buffer);
        Packet p = packetRegistry.createPacket(packetId);
        p.decode(buffer);
        return p;
    }

}
