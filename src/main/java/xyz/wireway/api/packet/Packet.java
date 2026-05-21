package xyz.wireway.api.packet;

public interface Packet {

    public int getId();

    public void decode(byte[] data);

    public byte[] getData();



}
