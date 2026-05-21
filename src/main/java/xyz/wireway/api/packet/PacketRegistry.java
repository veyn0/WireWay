package xyz.wireway.api.packet;

public interface PacketRegistry {

    public void registerPacket(Packet p);

    public Packet createPacket(int id);

}
