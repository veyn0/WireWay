package xyz.wireway.service;

import xyz.wireway.frame.datasource.DataSourceRegistry;
import xyz.wireway.frame.datasource.Provider;
import xyz.wireway.frame.transmit.FramedDataInput;
import xyz.wireway.protocol.Packet;
import xyz.wireway.protocol.PacketRegistry;
import xyz.wireway.transport.Transport;

public class WireWay implements Provider {

    private Transport transport;

    private FramedDataInput framedDataInput;

    public WireWay(Transport transport, DataSourceRegistry dataSourceRegistry, ){
        dataSourceRegistry.setProvider(this);
        framedDataInput = new FramedDataInput(dataSourceRegistry);

    }


    @Override
    public void onPacketReceive(Packet p) {

    }

    @Override
    public PacketRegistry getPacketRegistry() {
        return null;
    }
}
