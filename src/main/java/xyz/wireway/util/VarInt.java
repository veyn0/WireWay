package xyz.wireway.util;

import java.nio.BufferUnderflowException;
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

    public static int readVarIntSafe(ByteBuffer buf) {
        int startPosition = buf.position();
        int result = 0;

        for (int shift = 0; shift < 32; shift += 7) {
            if (!buf.hasRemaining()) {
                buf.position(startPosition);
                throw new BufferUnderflowException();
            }
            byte b = buf.get();
            result |= (b & 0x7F) << shift;
            if ((b & 0x80) == 0) {
                return result;
            }
        }

        buf.position(startPosition);
        throw new IllegalStateException("VarInt too long");
    }

}
