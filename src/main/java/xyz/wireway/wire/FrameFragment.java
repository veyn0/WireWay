package xyz.wireway.wire;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/**
 * One fragment inside a frame: {@code [streamId : VarInt][flags : byte][length : VarInt][data]}.
 * Flag 0x02 marks the first fragment of a stream, 0x01 the last.
 */
public final class FrameFragment {

    public static final int MAX_HEADER_BYTES = VarInt.MAX_BYTES + 1 + VarInt.MAX_BYTES;

    private static final byte FLAG_START = 0x02;
    private static final byte FLAG_END = 0x01;

    private final int streamId;
    private final byte flags;
    private final ByteBuffer data;

    public FrameFragment(int streamId, byte flags, ByteBuffer data) {
        this.streamId = streamId;
        this.flags = flags;
        this.data = data;
    }

    public void write(ByteBuffer out) {
        VarInt.writeVarInt(out, streamId);
        out.put(flags);
        VarInt.writeVarInt(out, data.remaining());
        out.put(data.duplicate());
    }

    static FrameFragment parse(ByteBuffer in) {
        try {
            int streamId = VarInt.readVarInt(in);
            byte flags = in.get();
            int length = VarInt.readVarInt(in);
            if (streamId < 0) throw new ProtocolException("negative stream id: " + streamId);
            if (length < 0 || length > in.remaining()) {
                throw new ProtocolException("fragment length exceeds frame payload");
            }
            ByteBuffer data = in.slice(in.position(), length);
            in.position(in.position() + length);
            return new FrameFragment(streamId, flags, data);
        } catch (BufferUnderflowException | IllegalStateException e) {
            throw new ProtocolException("truncated or malformed fragment header", e);
        }
    }

    public int encodedLength() {
        return VarInt.sizeOf(streamId) + 1 + VarInt.sizeOf(data.remaining()) + data.remaining();
    }

    public int streamId() {
        return streamId;
    }

    public ByteBuffer data() {
        return data;
    }

    public boolean isStart() {
        return (flags & FLAG_START) != 0;
    }

    public boolean isEnd() {
        return (flags & FLAG_END) != 0;
    }

    public static byte encodeFlags(boolean start, boolean end) {
        return (byte) ((start ? FLAG_START : 0) | (end ? FLAG_END : 0));
    }
}
