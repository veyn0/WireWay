package xyz.wireway.api.packet;

public interface PacketReceiver {

    public Packet onPacketReceive(Packet packet);

}
