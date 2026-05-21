package xyz.wireway.api.builder;

import xyz.wireway.api.server.Server;
import xyz.wireway.util.Constants;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ServerBuilder {

    private int port = Constants.DEFAULT_PORT;
    private List<InetAddress> listenAddresses = new ArrayList<>();

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

    public Server create(){
        //TODO
        return null;
    }
}
