package xyz.wireway.wire;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/** A length-prefixed group of fragments: {@code [payloadLength : VarInt][fragments...]}. */
public final class Frame {

    private final List<FrameFragment> fragments;

    public Frame(List<FrameFragment> fragments) {
        this.fragments = List.copyOf(fragments);
    }

    public List<FrameFragment> fragments() {
        return fragments;
    }

    public int payloadLength() {
        int length = 0;
        for (FrameFragment f : fragments) length += f.encodedLength();
        return length;
    }

    public int encodedLength() {
        return LengthPrefixed.recordSize(payloadLength());
    }

    public void write(ByteBuffer out) {
        VarInt.writeVarInt(out, payloadLength());
        for (FrameFragment f : fragments) f.write(out);
    }

    /** Parses a frame payload (the bytes after the length prefix) into its fragments. */
    public static Frame parse(ByteBuffer payload) {
        List<FrameFragment> fragments = new ArrayList<>();
        while (payload.hasRemaining()) {
            fragments.add(FrameFragment.parse(payload));
        }
        return new Frame(fragments);
    }
}
