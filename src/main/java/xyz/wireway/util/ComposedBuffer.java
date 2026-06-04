package xyz.wireway.util;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;

public class ComposedBuffer {

    private final ArrayDeque<ByteBuffer> buffers = new ArrayDeque<>();
    private int totalBytes = 0;

    public void add(ByteBuffer buffer) {
        buffers.add(buffer.slice());
        totalBytes += buffer.remaining();
    }

    public ByteBuffer get() {
        ByteBuffer result = ByteBuffer.allocate(totalBytes);
        while (!buffers.isEmpty()) {
            result.put(buffers.poll());
        }
        totalBytes = 0;
        result.flip();
        return result;
    }

    public ByteBuffer get(int length) {
        ByteBuffer result = ByteBuffer.allocate(length);
        int totalLength = 0;
        while (totalLength < length && !buffers.isEmpty()) {
            ByteBuffer b = buffers.peek();
            int currentLength = Math.min(b.remaining(), length - totalLength);
            result.put(b.slice(b.position(), currentLength));
            b.position(b.position() + currentLength);
            if (!b.hasRemaining()) buffers.poll();
            totalLength += currentLength;
        }
        totalBytes = Math.max(totalBytes - totalLength, 0);
        result.flip();
        return result;
    }

    public ByteBuffer peek(int length) {
        ByteBuffer result = ByteBuffer.allocate(length);
        int totalLength = 0;
        for (ByteBuffer buf : buffers) {
            if (totalLength >= length) break;
            int currentLength = Math.min(buf.remaining(), length - totalLength);
            result.put(buf.slice(buf.position(), currentLength));
            totalLength += currentLength;
        }
        result.flip();
        return result;
    }

    public int remaining() {
        return totalBytes;
    }
}