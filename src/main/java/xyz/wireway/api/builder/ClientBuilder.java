package xyz.wireway.api.builder;

import xyz.wireway.api.client.Client;
import xyz.wireway.util.Constants;

import java.net.InetAddress;

import java.net.InetAddress;
import java.util.UUID;

public class ClientBuilder {

    private int port = Constants.DEFAULT_PORT;
    private InetAddress address;
    private UUID endpointId = UUID.randomUUID();

    public ClientBuilder port(int port){
        this.port = port;
        return this;
    }

    public ClientBuilder address(InetAddress address){
        this.address = address;
        return this;
    }


    public ClientBuilder endpointId(UUID endpointId){
        this.endpointId = endpointId;
        return this;
    }

    public Client connect(){
        //TODO
        if (address == null) throw new IllegalArgumentException("address cannot be null");
        return null;
    }

}
