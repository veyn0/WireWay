package xyz.wireway.util;

import java.util.BitSet;

public final class IdAllocator {
    private final BitSet inUse = new BitSet();
    private final Object lock = new Object();

    public int allocate() {
        synchronized (lock) {
            int id = inUse.nextClearBit(0);
            inUse.set(id);
            return id;
        }
    }

    public void release(int id) {
        synchronized (lock) {
            inUse.clear(id);
        }
    }
}