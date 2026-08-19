package xyz.wireway.service;

import xyz.wireway.protocol.Packet;

/** Receives fire-and-forget packets sent via {@link WireWay#sendPacket}. */
public interface AsyncPacketListener {

    void onPacketReceive(Packet packet);
}
