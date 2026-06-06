package xyz.wireway.service.asyncpacketstream;

import xyz.wireway.protocol.Packet;

public interface  AsyncPacketListener {

    void onPacketReceive(Packet p);

}
