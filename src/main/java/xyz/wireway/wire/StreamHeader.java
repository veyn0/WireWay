package xyz.wireway.wire;

import xyz.wireway.util.ComposedBuffer;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/**
 * First bytes of every stream: {@code [channelTypeId : VarInt][endpointId : VarInt]}.
 */
public record StreamHeader(int channelTypeId, int endpointId) {

    public ByteBuffer encode() {
        ByteBuffer out = ByteBuffer.allocate(VarInt.sizeOf(channelTypeId) + VarInt.sizeOf(endpointId));
        VarInt.writeVarInt(out, channelTypeId);
        VarInt.writeVarInt(out, endpointId);
        return out.flip();
    }

    /** Decodes and consumes a header, or returns null while it is still incomplete. */
    public static StreamHeader tryDecode(ComposedBuffer in) {
        ByteBuffer view = in.peek(in.remaining());
        try {
            int channelTypeId = VarInt.readVarIntSafe(view);
            int endpointId = VarInt.readVarIntSafe(view);
            in.get(view.position());
            return new StreamHeader(channelTypeId, endpointId);
        } catch (BufferUnderflowException e) {
            return null;
        } catch (IllegalStateException e) {
            throw new ProtocolException("malformed stream header", e);
        }
    }
}
