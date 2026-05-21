package xyz.wireway.api.builder;

import xyz.wireway.api.client.Client;
import xyz.wireway.util.Constants;

import java.net.InetAddress;

import java.net.InetAddress;

public class ClientBuilder {

    private int port = Constants.DEFAULT_PORT;
    private InetAddress address;

    public ClientBuilder port(int port){
        this.port = port;
        return this;
    }

    public ClientBuilder address(InetAddress address){
        this.address = address;
        return this;
    }

    public Client connect(){
        //TODO
        if (address == null) throw new IllegalArgumentException("address cannot be null");
        return null;
    }

    public ClientBuilder address(InetAddress address){
        //TODO
        return this;
    }

    public ClientBuilder port(int port){
        //TODO
        return this;
    }

}
