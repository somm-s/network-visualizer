package com.lockedshields;

import java.util.Iterator;

import com.hickup.points.IPPoint;

public class DataBuffer implements Iterable<IPPoint> {

    public static final long MAX_PACKET_CAPACITY = 200000L;  // maximum number of packets to be visualized at once.

    private IPPoint[] data;
    public long timeInterval; // time interval that is shown in the canvas (microseconds)
    public long startTime; // start time of the interval in microseconds
    public long firstPacketTime;
    public long lastPacketTime;

    private int leftIndex;
    private int rightIndex;

    private boolean dataLoaded = false;

    public IPPoint[] getData() {
        return data;
    }

    public void setData(IPPoint[] data) {
        this.data = data;
        dataLoaded = true;

        // TODO: calculate first and last time and set accordingly.
        // TODO: if a time interval was set previously and the amount of packets is small enough, keep it.
    }

    public void setStartTimeDelta(int delta) {
        // TODO: iterate over array to the side of the delta until time delta is included
        // set the new start time for the interval.
        
        // TODO: notify observers.
    }

    public void setTimeInterval(long newTimeInterval) {
        // TODO: check current positions of indices and expand / reduce on both sides.
        // set the new time interval.

        // TODO: notify observers that something changed.
    }

    @Override
    public Iterator<IPPoint> iterator() {
        return new IPPointIterator();
    }

    // custom iterator to abstract hide the fact that not the entire array is iterated over.
    private class IPPointIterator implements Iterator<IPPoint> {

        private int currentIndex = leftIndex;

        @Override
        public boolean hasNext() {
            return dataLoaded && currentIndex < rightIndex;
        }

        @Override
        public IPPoint next() {
            if (hasNext()) {
                IPPoint element = data[currentIndex++];
                return element;
            } else {
                // Handle NoSuchElementException or return null
                throw new java.util.NoSuchElementException();
            }
        }
    }
}
