package xyz.wireway.transmit.outbound;

import xyz.wireway.transport.ConnectionState;
import xyz.wireway.transport.Transport;
import xyz.wireway.wire.Frame;

import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * The single outbound thread: assembles frames and writes them to the transport,
 * parking when no stream has data until {@link #signal()} wakes it.
 */
public final class TransmitLoop {

    private final Transport transport;
    private final StreamRegistry streams;
    private final FrameAssembler assembler;
    private final Object wake = new Object();
    private final Thread thread;
    private volatile boolean running = true;

    public TransmitLoop(Transport transport, StreamRegistry streams, FrameAssembler assembler) {
        this.transport = Objects.requireNonNull(transport, "transport");
        this.streams = streams;
        this.assembler = assembler;
        this.thread = new Thread(this::run, "wireway-transmit");
        this.thread.setDaemon(true);
    }

    public void start() {
        thread.start();
    }

    /** Wakes the loop after new data was made available. Never call while holding a source's lock. */
    public void signal() {
        synchronized (wake) {
            wake.notifyAll();
        }
    }

    public void close() {
        running = false;
        signal();
    }

    private void run() {
        while (running && transport.getConnectionState() != ConnectionState.CLOSED) {
            Frame frame = assembler.next();
            if (frame == null) {
                synchronized (wake) {
                    if (running && !streams.hasPending()) {
                        try {
                            wake.wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                }
                continue;
            }
            ByteBuffer data = ByteBuffer.allocate(frame.encodedLength());
            frame.write(data);
            data.flip();
            try {
                transport.send(data);
            } catch (RuntimeException e) {
                transport.close();
                return;
            }
        }
    }
}
