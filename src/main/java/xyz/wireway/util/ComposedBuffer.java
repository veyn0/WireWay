package xyz.wireway.util;

import java.nio.ByteBuffer;

public final class ComposedBuffer {

    private byte[] data;
    private int readIndex;
    private int writeIndex;

    public ComposedBuffer() {
        this(1024);
    }

    public ComposedBuffer(int initialCapacity) {
        this.data = new byte[Math.max(initialCapacity, 16)];
    }

    public int remaining() {
        return writeIndex - readIndex;
    }

    public void add(ByteBuffer source) {
        int needed = source.remaining();
        if (needed == 0) return;
        if (readIndex == writeIndex) {
            readIndex = 0;
            writeIndex = 0;
        }
        ensureWritable(needed);
        source.get(data, writeIndex, needed);
        writeIndex += needed;
    }

    public ByteBuffer peek(int length) {
        checkAvailable(length);
        return ByteBuffer.wrap(data, readIndex, length).slice().asReadOnlyBuffer();
    }

    public ByteBuffer get(int length) {
        checkAvailable(length);
        ByteBuffer view = ByteBuffer.wrap(data, readIndex, length).slice().asReadOnlyBuffer();
        readIndex += length;
        return view;
    }

    public ByteBuffer get() {
        return get(remaining());
    }

    private void checkAvailable(int length) {
        if (length < 0) {
            throw new IllegalArgumentException("length < 0: " + length);
        }
        if (length > remaining()) {
            throw new IllegalArgumentException(
                    "Not enough bytes: requested " + length + ", have " + remaining());
        }
    }

    private void ensureWritable(int additional) {
        if (writeIndex + additional <= data.length) {
            return;
        }
        int unread = writeIndex - readIndex;
        if (unread + additional <= data.length) {
            System.arraycopy(data, readIndex, data, 0, unread);
        } else {
            int newCapacity = Math.max(data.length * 2, unread + additional);
            byte[] newData = new byte[newCapacity];
            System.arraycopy(data, readIndex, newData, 0, unread);
            data = newData;
        }
        readIndex = 0;
        writeIndex = unread;
    }
}