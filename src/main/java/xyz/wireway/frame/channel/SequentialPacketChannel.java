package xyz.wireway.frame.channel;

import xyz.wireway.protocol.Packet;
import xyz.wireway.service.PacketInfo;
import xyz.wireway.service.packetstream.PacketChannelContext;
import xyz.wireway.util.ComposedBuffer;
import xyz.wireway.util.VarInt;

import java.nio.ByteBuffer;

@ChannelId("xyz.wireway.system.channel.sequentialpacketchannel")
public class SequentialPacketChannel extends ComposedBufferBase implements Channel<PacketChannelContext> {

    private PacketChannelContext context;

    private int subId;

    public SequentialPacketChannel() {
    }

    public void addPacket(Packet p, boolean isRequest, int id){
        ByteBuffer metaData = ByteBuffer.allocate(6);
        metaData.put((byte) (isRequest ? 0x01 : 0x00));
        VarInt.writeVarInt(metaData, id);
        composedBuffer.add(metaData.flip());
        composedBuffer.add(Packet.getData(p, context.getPacketRegistry()));
    }

    @Override
    protected void postWrite() {
        while (canRead(composedBuffer)){

            int len = Math.min(11, composedBuffer.remaining());
            ByteBuffer buffer1 = composedBuffer.peek(len);
            boolean isRequest = (buffer1.get() & 0x01) ==0x01;
            int referenceId = VarInt.readVarInt(buffer1);

            composedBuffer.get(1 + VarInt.sizeOf(referenceId));
            Packet p = Packet.read(composedBuffer, context.getPacketRegistry());
            PacketInfo info = new PacketInfo(isRequest, referenceId, subId, p);
            context.getOnPacketReceivedAtSubChannel().accept(info);
        }
    }

    private boolean canRead(ComposedBuffer buffer){
        try {
            int len = Math.min(11, buffer.remaining());
            if(len<4) return false;
            ByteBuffer buffer1 = buffer.peek(len);
            boolean isRequest = (buffer1.get() & 0x01) ==0x01;
            int referenceId = VarInt.readVarInt(buffer1);
            int length = VarInt.readVarInt(buffer1);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    @Override
    protected void postClose() {

    }

    @Override
    public boolean isExhausted() {
        return false;
    }

    @Override
    public void inject(PacketChannelContext context) {
        this.context = context;
    }

    @Override
    public void setSubId(int subId) {
        this.subId = subId;
    }

    @Override
    public int getSubId() {
        return subId;
    }


}
