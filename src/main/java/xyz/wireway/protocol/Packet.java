package xyz.wireway.protocol;

import java.nio.ByteBuffer;

public interface Packet {

    Packet decode(ByteBuffer buffer);

    ByteBuffer encode(Packet p);

}
