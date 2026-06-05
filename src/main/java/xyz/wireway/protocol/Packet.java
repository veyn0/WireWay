package xyz.wireway.protocol;

import xyz.wireway.util.ComposedBuffer;
import xyz.wireway.util.VarInt;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public interface Packet {

    Packet decode(ByteBuffer buffer);

    ByteBuffer encode(Packet p);

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
