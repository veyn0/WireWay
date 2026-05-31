package xyz.wireway.api;


import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface Connection {

    public UUID getEndpointId();

    public boolean isActive();

    //public CompletableFuture<Packet> sendPacket(Packet packet);

}
