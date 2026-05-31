package xyz.wireway.frame;

import xyz.wireway.util.VarInt;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class Frame {

    private List<FrameFragment> fragments;

    public Frame(List<FrameFragment> fragments) {
        this.fragments = fragments;
    }

    public void write(ByteBuffer buffer){
        int length = 0;
        for(FrameFragment f : fragments) length += f.length();
        VarInt.writeVarInt(buffer, length);
        for(FrameFragment f : fragments){
            f.write(buffer);
        }
    }

    public static Frame read(ByteBuffer buffer, int length){
        if (buffer.remaining() < length) {
            throw new IllegalArgumentException("buffer size cannot be less than Frame length");
        }
        int totalLength = 0;
        List<FrameFragment> frameFragments = new ArrayList<>();
        while (totalLength < length){
            FrameFragment fragment = FrameFragment.read(buffer);
            totalLength += fragment.length();
            frameFragments.add(fragment);
        }
        return new Frame(frameFragments);
    }

}
