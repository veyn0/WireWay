package xyz.wireway.service;

import xyz.wireway.protocol.Packet;

/** Handles inbound requests on a packet stream; a non-null return is sent back as the response. */
public interface PacketListener {

    Packet onPacketReceive(Packet request);
}
