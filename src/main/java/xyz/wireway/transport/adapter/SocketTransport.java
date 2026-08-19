package xyz.wireway.transport.adapter;

import xyz.wireway.transport.ConnectionState;
import xyz.wireway.transport.Transport;
import xyz.wireway.transport.TransportListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class SocketTransport implements Transport {

    private final Socket socket;
    private final List<TransportListener> listeners = new CopyOnWriteArrayList<>();
    private final AtomicBoolean started = new AtomicBoolean();
    private final AtomicBoolean closed = new AtomicBoolean();

    public SocketTransport(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void start() {
        if (!started.compareAndSet(false, true)) return;
        Thread thread = new Thread(this::receiveLoop, "wireway-receive");
        thread.setDaemon(true);
        thread.start();
    }

    private void receiveLoop() {
        byte[] chunk = new byte[4096];
        try {
            InputStream in = socket.getInputStream();
            int bytesRead;
            while ((bytesRead = in.read(chunk)) != -1) {
                ByteBuffer data = ByteBuffer.wrap(Arrays.copyOf(chunk, bytesRead));
                for (TransportListener listener : listeners) {
                    listener.onReceive(data.duplicate());
                }
            }
        } catch (IOException e) {
            if (!closed.get()) notifyError(e);
        } catch (RuntimeException e) {
            notifyError(e);
        } finally {
            close();
        }
    }

    @Override
    public void send(ByteBuffer data) {
        if (closed.get()) throw new IllegalStateException("transport is closed");
        try {
            byte[] bytes = new byte[data.remaining()];
            data.duplicate().get(bytes);
            socket.getOutputStream().write(bytes);
        } catch (IOException e) {
            close();
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void addListener(TransportListener listener) {
        listeners.add(listener);
    }

    @Override
    public ConnectionState getConnectionState() {
        return closed.get() ? ConnectionState.CLOSED : ConnectionState.CONNECTED;
    }

    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) return;
        try {
            socket.close();
        } catch (IOException ignored) {
        }
        for (TransportListener listener : listeners) {
            try {
                listener.onClosed();
            } catch (RuntimeException ignored) {
            }
        }
    }

    private void notifyError(Throwable cause) {
        for (TransportListener listener : listeners) {
            try {
                listener.onError(cause);
            } catch (RuntimeException ignored) {
            }
        }
    }

    /** Accepts connections until the server socket fails; one handler failure closes only that socket. */
    public static void listen(int port, Consumer<Transport> onConnect) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket socket = serverSocket.accept();
                try {
                    onConnect.accept(new SocketTransport(socket));
                } catch (RuntimeException e) {
                    try {
                        socket.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static SocketTransport connect(String host, int port) {
        try {
            return new SocketTransport(new Socket(host, port));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
