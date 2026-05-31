package xyz.wireway.util;

import java.nio.ByteBuffer;

public class VarInt {

    public static void writeVarInt(ByteBuffer buf, int value) {
        while ((value & ~0x7F) != 0) {
            buf.put((byte) ((value & 0x7F) | 0x80));
            value >>>= 7;
        }
        buf.put((byte) value);
    }

    public static int readVarInt(ByteBuffer buf) {
        int result = 0;
        for (int shift = 0; shift < 32; shift += 7) {
            byte b = buf.get();
            result |= (b & 0x7F) << shift;
            if ((b & 0x80) == 0) {
                return result;
            }
        }
        throw new IllegalStateException("VarInt too long");
    }

    public static int sizeOf(int value) {
        int significantBits = 32 - Integer.numberOfLeadingZeros(value);
        return Math.max(1, (significantBits + 6) / 7);
    }

}
