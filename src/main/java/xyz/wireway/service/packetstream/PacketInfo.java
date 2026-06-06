package xyz.wireway.service.packetstream;

import xyz.wireway.protocol.Packet;

public class PacketInfo {

    private final boolean isRequest;
    private final int referenceId, subId;
    private final Packet p;

    public PacketInfo(boolean isRequest, int referenceId, int subId, Packet p) {
        this.isRequest = isRequest;
        this.referenceId = referenceId;
        this.subId = subId;
        this.p = p;
    }

    public boolean isRequest() {
        return isRequest;
    }

    public int getReferenceId() {
        return referenceId;
    }

    public int getSubId() {
        return subId;
    }

    public Packet getP() {
        return p;
    }
}
