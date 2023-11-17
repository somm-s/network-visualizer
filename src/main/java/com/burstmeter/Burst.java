package com.burstmeter;

import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;
import com.hickup.points.IPPoint;

public class Burst {

    // the next burst in the sequence of bursts with the same hashmap key
    Burst next;
    Burst prev;

    private String srcHost;
    private String dstHost;
    private int srcPort;
    private int dstPort;
    private int protocol;

    private Timestamp startTime;
    private Timestamp endTime;
    private int numPackets;
    private int bytes;
    private Timestamp avgTime;

    private double x;
    private double y;

    private String connectionIdentifier;


    // every burst has a list of IPPoints
    private List<IPPoint> ipPoints = new LinkedList<IPPoint>();

    public Burst() {
        this.next = null;
    }

    public Burst(IPPoint p) {
        this(p.srcIp, p.dstIp, p.time, p.time, 0, 0, p.getSrcPort(), p.getDstPort(), p.getProtocol(), p.getConnectionIdentifier());
    }


    public Burst(String srcHost, String dstHost, Timestamp startTime, Timestamp endTime, int packets, int bytes, int srcPort, int dstPort, int protocol, String burstIdentifier) {
        this.srcHost = srcHost;
        this.dstHost = dstHost;
        this.startTime = startTime;
        this.endTime = endTime;
        this.numPackets = packets;
        this.bytes = bytes;
        this.srcPort = srcPort;
        this.dstPort = dstPort;
        this.protocol = protocol;
        this.connectionIdentifier = burstIdentifier;
        this.next = null;
        this.avgTime = new Timestamp(startTime.getTime());
    }

    public Burst getNext() {
        return next;
    }

    public void setNext(Burst next) {
        this.next = next;
    }

    public String getSrcHost() {
        return srcHost;
    }

    public void setSrcHost(String srcHost) {
        this.srcHost = srcHost;
    }

    public String getDstHost() {
        return dstHost;
    }

    public void setDstHost(String dstHost) {
        this.dstHost = dstHost;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }

    public long getDuration() {
        return endTime.getTime() - startTime.getTime();
    }

    public int getNumPackets() {
        return numPackets;
    }

    public void setNumPackets(int packets) {
        this.numPackets = packets;
    }

    public void addPackets(int packets) {
        this.numPackets += packets;
    }

    public int getBytes() {
        return bytes;
    }

    public void setBytes(int bytes) {
        this.bytes = bytes;
    }

    public void addBytes(int bytes) {
        this.bytes += bytes;
    }

    public int getSrcPort() {
        return srcPort;
    }

    public void setSrcPort(int srcPort) {
        this.srcPort = srcPort;
    }


    public int getDstPort() {
        return dstPort;
    }

    public void setDstPort(int dstPort) {
        this.dstPort = dstPort;
    }


    public String getConnectionIdentifier() {
        return connectionIdentifier;
    }

    public void setConnectionIdentifier(String burstIdentifier) {
        this.connectionIdentifier = burstIdentifier;
    }


    public int getProtocol() {
        return protocol;
    }

    public void setProtocol(int protocol) {
        this.protocol = protocol;
    }


    public List<IPPoint> getIpPoints() {
        return ipPoints;
    }

    public void setIpPoints(List<IPPoint> ipPoints) {
        this.ipPoints = ipPoints;
    }

    public Timestamp getAvgTime() {
        return avgTime;
    }

    public void setAvgTime(Timestamp avgTime) {
        this.avgTime = avgTime;
    }
    
    public void setPrev(Burst prev) {
        this.prev = prev;
    }

    public Burst getPrev() {
        return prev;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }



    // other methods

    public boolean sameDirection(IPPoint p) {
        if (p.srcIp.equals(this.srcHost) && p.dstIp.equals(this.dstHost) && p.getSrcPort() == this.srcPort && p.getDstPort() == this.dstPort && p.getProtocol() == this.protocol) {
            return true;
        }
        return false;
    }

    public boolean sameInteraction(IPPoint p) {
        if(p.getMicroseconds() > IPPoint.getMicroseconds(startTime) && p.getMicroseconds() < IPPoint.getMicroseconds(endTime) + Model.interactionTimeout) {
            // System.out.println(p.getMicroseconds() + "  " + IPPoint.getMicroseconds(startTime) + "  " + IPPoint.getMicroseconds(endTime) + "  " + Model.interactionTimeout);
            return true;
        }

        return false;
    }

    public boolean sameBurst(IPPoint p) {
        if(sameDirection(p) && (p.getMicroseconds() > IPPoint.getMicroseconds(startTime) && p.getMicroseconds() < IPPoint.getMicroseconds(endTime) + Model.burstTimeout)) {
            return true;
        }

        return false;
    }






    public void addPoint(IPPoint p) {
        this.ipPoints.add(p);

        // only add bytes and packet above a certain threshold:
        addBytes(p.packetSize);
        addPackets(1);;
        this.endTime = p.time;

        // average time is the mean of start and end time
        this.avgTime = new Timestamp((this.startTime.getTime() + this.endTime.getTime()) / 2);
        this.avgTime.setNanos((this.startTime.getNanos() + this.endTime.getNanos()) / 2);

    }



    @Override
    public String toString() {
        String s = "[" + bytes + "," + numPackets + ", " + this.endTime + " - " + this.startTime +  "] -> ";
        if (next != null) {
            s += next.toString();
        }

        return s;
    }



}
