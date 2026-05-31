package xyz.wireway.frame;

import xyz.wireway.util.VarInt;

import java.nio.ByteBuffer;

public class FrameFragment {

    /*
        [dataID : VarInt] [flag : byte] [dataLength : VarInt] [Data : bytes]
        flag bit 7 = isStart
        flag bit 8 = isEnd
    */

    private final int dataId;
    private final byte flag;
    private final ByteBuffer data;

    public FrameFragment(int dataId, byte flag, ByteBuffer data) {
        this.dataId = dataId;
        this.flag = flag;
        this.data = data;
    }

    public void write(ByteBuffer buffer){
        VarInt.writeVarInt(buffer, dataId);
        buffer.put(flag);
        VarInt.writeVarInt(buffer, data.remaining());
        buffer.put(data.duplicate());
    }

    public static FrameFragment read(ByteBuffer buffer) {
        int dataId = VarInt.readVarInt(buffer);
        byte flag = buffer.get();
        int length = VarInt.readVarInt(buffer);
        ByteBuffer data = buffer.slice(buffer.position(), length);
        buffer.position(buffer.position() + length);
        return new FrameFragment(dataId, flag, data);
    }

    public int length(){
        return VarInt.sizeOf(dataId) + 1 + VarInt.sizeOf(data.remaining())+ data.remaining();
    }

}
