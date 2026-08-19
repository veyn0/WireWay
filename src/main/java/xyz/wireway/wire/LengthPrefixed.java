package xyz.wireway.wire;

import xyz.wireway.util.ComposedBuffer;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/** The single place that knows the {@code [length : VarInt][payload]} record encoding. */
public final class LengthPrefixed {

    public static final int MAX_PREFIX_BYTES = VarInt.MAX_BYTES;

    /** Upper bound for any declared payload length; guards against unbounded buffering. */
    public static final int MAX_RECORD_BYTES = 1 << 20;

    private LengthPrefixed() {}

    /** Returns the declared payload length, or -1 while the prefix itself is still incomplete. */
    public static int peekLength(ComposedBuffer in) {
        ByteBuffer view = in.peek(Math.min(in.remaining(), MAX_PREFIX_BYTES));
        int length;
        try {
            length = VarInt.readVarIntSafe(view);
        } catch (BufferUnderflowException e) {
            return -1;
        } catch (IllegalStateException e) {
            throw new ProtocolException("malformed length prefix", e);
        }
        if (length < 0 || length > MAX_RECORD_BYTES) {
            throw new ProtocolException("record length out of bounds: " + length);
        }
        return length;
    }

    /** True once one complete record is buffered. */
    public static boolean hasRecord(ComposedBuffer in) {
        int length = peekLength(in);
        return length >= 0 && in.remaining() >= length + VarInt.sizeOf(length);
    }

    /** Consumes one complete record and returns its payload. Call only after {@link #hasRecord}. */
    public static ByteBuffer takeRecord(ComposedBuffer in) {
        int length = peekLength(in);
        in.get(VarInt.sizeOf(length));
        return in.get(length);
    }

    public static void writeRecord(ByteBuffer out, ByteBuffer payload) {
        VarInt.writeVarInt(out, payload.remaining());
        out.put(payload.duplicate());
    }

    public static int recordSize(int payloadLength) {
        return payloadLength + VarInt.sizeOf(payloadLength);
    }
}
