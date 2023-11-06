package com.lockedshields;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import com.hickup.points.IPPoint;

public class DataBuffer implements Iterable<IPPoint> {
    
    public static final long MAX_PACKET_CAPACITY = 30000;  // maximum number of packets to be visualized at once.


    // invariant: data is always sorted by time
    private IPPoint[] data;

    public long timeInterval = -1; // time interval that is shown in the canvas (microseconds)
    public long startTime; // start time of the interval in microseconds
    public long firstPacketTime;
    public long lastPacketTime;

    private int leftIndex;
    private int rightIndex;

    public String observer = "";
    String srcHost = "";
    String dstHost = "";
    long startTimeFilter = -1;
    long endTimeFilter = -1;

    boolean dataLoaded = false;

    private List<DataChangeListener> listeners = new LinkedList<>();

    public void setData(IPPoint[] data, String observer) {
        this.data = data;
        this.observer = observer;
        
        if(data.length == 0) {
            return;
        }

        // sort
        Arrays.sort(data);

        // calculate first and last time and set accordingly.
        firstPacketTime = data[0].getMicroseconds();
        lastPacketTime = data[data.length - 1].getMicroseconds();

        // if a time interval was set previously and the amount of packets is small enough, keep it.
        if(timeInterval != -1) {
            // go through the array and find the first packet that is in the time interval.
            for(int i = 0; i < data.length; i++) {
                if(data[i].getMicroseconds() >= startTime) {
                    leftIndex = i;
                    break;
                }
            }

            // go through the array and find the last packet that is in the time interval.
            for(int i = data.length - 1; i >= 0; i--) {
                if(data[i].getMicroseconds() <= startTime + timeInterval) {
                    rightIndex = i;
                    break;
                }
            }

            // if the amount of packets is too large, set the time interval to -1.
            if(rightIndex - leftIndex > MAX_PACKET_CAPACITY) {
                timeInterval = -1;
            }
        }

        // if no time interval was set previously, set the time interval as large as possible (s.t. less packets than MAX_PACKET_CAPACITY).
        if(timeInterval == -1) {
            timeInterval = lastPacketTime - firstPacketTime;
            startTime = firstPacketTime;
            leftIndex = 0;
            rightIndex = data.length - 1;
            while(rightIndex - leftIndex > MAX_PACKET_CAPACITY) {
                rightIndex--;
            }
            timeInterval = data[rightIndex].getMicroseconds() - data[leftIndex].getMicroseconds();
        }

        System.out.println("Set " + data.length + " packets");
        System.out.println(this);
        dataLoaded = true;
        fireEvent();
    }

    public String getObservedHost() {
        return observer;
    }

    public String toString() {
        return "Data loaded: firstPacketTime: " + firstPacketTime + " lastPacketTime: " + lastPacketTime + " timeInterval: " + timeInterval + " startTime: " + startTime + " leftIndex: " + leftIndex + " rightIndex: " + rightIndex;
    }

    public void setScrollDelta(double delta) {
        if(!dataLoaded) {
            return;
        }


        long lastInterval = timeInterval;

        if(delta > 0) {
            timeInterval = (long) (1.111111 * timeInterval);
        } else if (delta < 0) {
            timeInterval = (long) (0.9 * timeInterval);
        }



        // adjust the start time so that the middle of the time interval stays the same
        startTime += (lastInterval - timeInterval) / 2;
        adjustIndices();
        fireEvent();
    }

    private void adjustIndices() {
        // use binary search to find the first packet that is in the time interval
        int left = 0;
        int right = data.length - 1;
        while (left <= right) {
            int mid = (left + right) / 2;
            if (data[mid].getMicroseconds() < startTime) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        leftIndex = left;

        // use binary search to find the last packet that is in the time interval
        left = 0;
        right = data.length - 1;
        while (left <= right) {
            int mid = (left + right) / 2;
            if (data[mid].getMicroseconds() > startTime + timeInterval) {
                right = mid - 1;
            } else {
                left = mid + 1;
            }
        }
        rightIndex = right;
    }

    public void setStartTime(long newStartTime) {   
        if(!dataLoaded) {
            return;
        }     
        startTime = newStartTime;
        adjustIndices();
        fireEvent();
    }

    public void filterData(String startTimeString, String endTimeString, String observer, String dstHost) {
        // if(startTimeString.equals("")) {
        //     this.startTimeFilter = -1;
        // } else {
        //     this.startTimeFilter = IPPoint.getMicroseconds(IPPoint.timeFromString(startTimeString));
        // }

        // if(endTimeString.equals("")) {
        //     this.endTimeFilter = -1;
        // } else {
        //     this.endTimeFilter = IPPoint.getMicroseconds(IPPoint.timeFromString(endTimeString));
        // }

        this.observer = observer;
        this.dstHost = dstHost;
        // TODO: should also adjust indices.
        fireEvent();
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

            // // also check if filter is set and if the current element is in the filter
            // if(dataLoaded && data[rightIndex].getMicroseconds() > endTimeFilter) {
            //     return false;
            // }

            return dataLoaded && currentIndex <= rightIndex;
        }

        @Override
        public IPPoint next() {
            if (hasNext()) {
                IPPoint element = data[currentIndex++];

                // check for filter TODO: use separate filter class or function
                // if (startTimeFilter != -1 && element.getMicroseconds() < startTimeFilter) {
                //     return next();
                // }

                if (!observer.equals("") && !(element.srcIp.equals(observer) || element.dstIp.equals(observer))) { // TODO: very ugly solution. please build filter class!
                    if(hasNext())
                        return next();
                }

                // if (!dstHost.equals("") && !element.srcIp.equals(dstHost) && !element.dstIp.equals(dstHost)) {
                //     return next();
                // }

                return element;
            } else {
                // Handle NoSuchElementException or return null
                throw new java.util.NoSuchElementException();
            }
        }
    }

    public void addListener(DataChangeListener listener) {
        listeners.add(listener);
    }

    private void fireEvent() {
        for (DataChangeListener listener : listeners) {
            listener.onDataChanged();
        }
    }

    public interface DataChangeListener {
        void onDataChanged();
    }

    public IPPoint getHoveredPoint(double mouseX, double mouseY) {
        Iterator<IPPoint> it = this.iterator();

        while (it.hasNext()) {
            IPPoint point = it.next();
            // Implement logic to check if the mouse coordinates are within the bounds of the point
            if (pointIsHovered(point, mouseX, mouseY)) {
                return point;
            }
        }

        return null;
    }

    private boolean pointIsHovered(IPPoint point, double mouseX, double mouseY) {
        // Implement logic to check if the mouse coordinates are within the bounds of the point
        // You might use the x and y coordinates of the point and some radius to define the bounds
        return (Math.abs(mouseX - point.getX()) < 4) && (Math.abs(mouseY - point.getY()) < 4);
    }
}
