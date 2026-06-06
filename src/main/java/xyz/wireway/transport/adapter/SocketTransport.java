package xyz.wireway.transport.adapter;

import xyz.wireway.Main;
import xyz.wireway.service.ConnectionListener;
import xyz.wireway.transport.ConnectionState;
import xyz.wireway.transport.Transport;
import xyz.wireway.transport.TransportListener;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SocketTransport implements Transport {

    private final Socket socket;
    private final List<TransportListener> transportListeners = new ArrayList<>();
    private ConnectionState connectionState;

    public SocketTransport(Socket socket) {
        this.socket = socket;
        this.connectionState = ConnectionState.CONNECTED;
        startReceiveLoop();
    }

    @Override
    public void send(ByteBuffer data) {
        try {
            if (data.hasArray()) {
                socket.getOutputStream().write(
                        data.array(),
                        data.arrayOffset() + data.position(),
                        data.remaining()
                );
            } else {
                byte[] bytes = new byte[data.remaining()];
                data.get(bytes);
                socket.getOutputStream().write(bytes);
            }
        } catch (IOException e) {
            close();
            throw new RuntimeException(e);
        }
    }

    private void startReceiveLoop() {
        Thread t = new Thread(() -> {
            byte[] buffer = new byte[4096];
            try {
                int bytesRead;
                while ((bytesRead = socket.getInputStream().read(buffer)) != -1) {
                    receive(ByteBuffer.wrap(buffer, 0, bytesRead));
                }
            } catch (SocketException ignored) {
                // Socket wurde geschlossen
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                close();
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void receive(ByteBuffer buffer){
        Main.printByteBufferNoFlip(buffer.duplicate());
        for(TransportListener l : transportListeners){
            l.onReceive(buffer.duplicate());
        }
    }

    @Override
    public void addListener(TransportListener listener) {
        transportListeners.add(listener);
    }

    @Override
    public ConnectionState getConnectionState() {
        return connectionState;
    }

    @Override
    public void close() {
        this.connectionState = ConnectionState.CLOSED;
    }

    public static void listen(int port, Consumer<Transport> onConnect){
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true){
                Socket socket = serverSocket.accept();
                onConnect.accept(new SocketTransport(socket));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static SocketTransport connect(String host, int port){
        try {
            return new SocketTransport(new Socket(host, port));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
