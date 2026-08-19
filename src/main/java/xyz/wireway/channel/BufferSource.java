package xyz.wireway.channel;

import java.nio.ByteBuffer;

/** One-shot source over a fixed buffer; complete once drained. */
public final class BufferSource implements DataSource {

    private final ByteBuffer data;

    public BufferSource(ByteBuffer data) {
        this.data = data;
    }

    @Override
    public int available() {
        return data.remaining();
    }

    @Override
    public void read(ByteBuffer dst, int length) {
        int limit = data.limit();
        data.limit(data.position() + length);
        dst.put(data);
        data.limit(limit);
    }

    @Override
    public boolean isComplete() {
        return true;
    }
}
