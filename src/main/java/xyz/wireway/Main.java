package xyz.wireway;

import xyz.wireway.frame.datasource.ByteArrayDatasource;
import xyz.wireway.frame.datasource.DataSource;
import xyz.wireway.frame.receive.FramedDataInput;
import xyz.wireway.frame.send.FramedDataOutput;
import xyz.wireway.transport.Transport;
import xyz.wireway.transport.adapter.LoopbackTransport;
import xyz.wireway.transport.listener.DebugtransportListener;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) {
        Transport t = LoopbackTransport.connect();
        t.addListener(new DebugtransportListener());
        t.addListener(new FramedDataInput());
        FramedDataOutput fdOut = new FramedDataOutput(t, 1024, 10240);
        byte[] data = {125, 126, 127, 1,1,1,1, 2,1,1,1, 3,1,1,1, 4,1,1,1, 5,1,1,1, 6,1,1,1, 7,1,1,1, 8,1,1,1, 9,1,1,1, 10,1,1,1};
        DataSource dataSource = new ByteArrayDatasource(data.clone());
        DataSource dataSource1 = new ByteArrayDatasource(data.clone());
        DataSource dataSource2 = new ByteArrayDatasource(data.clone());
        DataSource dataSource3 = new ByteArrayDatasource(data.clone());
        DataSource dataSource4 = new ByteArrayDatasource(data.clone());
        fdOut.addDataSource(dataSource);
        fdOut.addDataSource(dataSource1);
        fdOut.addDataSource(dataSource2);
        fdOut.addDataSource(dataSource3);
        fdOut.addDataSource(dataSource4);

        fdOut.sendFrame();



    }

    public static void printByteBuffer(ByteBuffer data){

        ByteBuffer buffer = data.duplicate();
        buffer.flip();
        System.out.println("[DEBUG] ByteBuffer content:");
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        System.out.println(Arrays.toString(bytes));
    }


    public static void printByteBufferNoFlip(ByteBuffer data){

        ByteBuffer buffer = data.duplicate();
        System.out.println("[DEBUG] ByteBuffer content:");
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        System.out.println(Arrays.toString(bytes));
    }

}