package xyz.wireway.api.builder;

import xyz.wireway.api.server.Server;
import xyz.wireway.util.Constants;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class ServerBuilder {

    private int port = Constants.DEFAULT_PORT;
    private List<InetAddress> listenAddresses = new ArrayList<>();
    private UUID endpointId = UUID.randomUUID();

    public ServerBuilder port(int port){
        this.port = port;
        return this;
    }

    public ServerBuilder listenAddress(InetAddress address){
        this.listenAddresses.add(address);
        return this;
    }

    public ServerBuilder listenAddress(Collection<InetAddress> addresses){
        this.listenAddresses.addAll(addresses);
        return this;
    }

    public ServerBuilder endpointId(UUID endpointId){
        this.endpointId = endpointId;
        return this;
    }

    public Server create(){
        //TODO
        return null;
    }
}
