package xyz.wireway.protocol;

public interface Packet {

    public Packet decode(byte[] data);

    public byte[] encode(Packet p);

}
