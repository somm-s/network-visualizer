package ch.cydcampus.hickup.util;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class MultipleReaderRingBuffer {
    private final int bufferSize;
    private final Object[] buffer;
    private final AtomicInteger writeIndex = new AtomicInteger(0);
    private HashMap<Long, AtomicInteger> readIndexes;

    public MultipleReaderRingBuffer(int bufferSize) {
        this.bufferSize = bufferSize;
        this.buffer = new Object[bufferSize];
        this.readIndexes = new HashMap<Long, AtomicInteger>();
    }

    public void registerReader() {
        System.out.println("Registering reader" + Thread.currentThread().getId());
        long threadId = Thread.currentThread().getId();
        readIndexes.put(threadId, new AtomicInteger(0));
    }

    /*
     * Put element into the buffer, even if full. Returns true if the buffer was already full.
     */
    public boolean produce(Object item) {
        boolean isOverflow = false;
        int nextWriteIndex = (writeIndex.get() + 1) % bufferSize;

        // check if running over one of the read indexes
        for (AtomicInteger readIndex : readIndexes.values()) {
            if (nextWriteIndex == readIndex.get()) {
                isOverflow = true;
                break;
            }
        }
        buffer[writeIndex.get()] = item;
        writeIndex.set(nextWriteIndex);

        return isOverflow;
    }

    public Object consume() throws InterruptedException {
        long threadId = Thread.currentThread().getId();
        int readIndex = readIndexes.get(threadId).get();
        while (readIndex == writeIndex.get()) {
            Thread.sleep(1); // change to use wait/notify
        }

        Object item = buffer[readIndex];
        int nextReadIndex = (readIndex + 1) % bufferSize;
        readIndexes.get(threadId).set(nextReadIndex);

        return item;
    }
}
