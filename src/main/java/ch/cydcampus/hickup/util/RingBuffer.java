package ch.cydcampus.hickup.util;

import java.util.concurrent.atomic.AtomicInteger;

/*
 * Ring buffer for asynchronous communication between threads.
 * Non-blocking. Single producer, multiple consumers.
 */
public class RingBuffer {
    private final int bufferSize;
    private final Object[] buffer;
    private final AtomicInteger readIndex = new AtomicInteger(0);
    private final AtomicInteger writeIndex = new AtomicInteger(0);

    public RingBuffer(int bufferSize) {
        this.bufferSize = bufferSize;
        this.buffer = new Object[bufferSize];
    }

    public boolean produce(Object item) {
        return true;
    }

    public Object consume() throws InterruptedException {
        return null;
    }
}
