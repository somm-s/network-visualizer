package com.capture;

import java.util.concurrent.atomic.AtomicInteger;

public class SynchronizedRingBuffer {
    private final int bufferSize;
    private final Object[] buffer;
    private final AtomicInteger readIndex = new AtomicInteger(0);
    private final AtomicInteger writeIndex = new AtomicInteger(0);

    public SynchronizedRingBuffer(int bufferSize) {
        this.bufferSize = bufferSize;
        this.buffer = new Object[bufferSize];
    }

    // If the buffer is full, don't block the thread, but return false
    public boolean produce(Object item) {
        int nextWriteIndex;
        if ((nextWriteIndex = (writeIndex.get() + 1) % bufferSize) == readIndex.get()) {
            return false;
        }

        buffer[writeIndex.get()] = item;
        writeIndex.set(nextWriteIndex);

        return true;
    }

    public Object consume() throws InterruptedException {
        while (readIndex.get() == writeIndex.get()) {
            Thread.sleep(1);
        }

        Object item = buffer[readIndex.get()];
        int nextReadIndex = (readIndex.get() + 1) % bufferSize;
        readIndex.set(nextReadIndex);

        return item;
    }
}
