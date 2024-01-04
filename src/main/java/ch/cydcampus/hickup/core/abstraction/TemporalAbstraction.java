package ch.cydcampus.hickup.core.abstraction;

import java.util.concurrent.ConcurrentLinkedDeque;

import ch.cydcampus.hickup.core.Packet;
import ch.cydcampus.hickup.util.TimeInterval;

public class TemporalAbstraction implements Abstraction {

    private int layer;
    private long bytes;
    private TimeInterval timeInterval;
    private ConcurrentLinkedDeque<Abstraction> children;
    private Packet newestPacket;

    public TemporalAbstraction(int layer) {
        this.layer = layer;
        this.timeInterval = new TimeInterval();
        this.bytes = 0;
        this.children = new ConcurrentLinkedDeque<Abstraction>();
    }

    @Override
    public boolean isRoot() {
        return layer == 0;
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    @Override
    public int getLayer() {
        return layer;
    }

    @Override
    public TimeInterval getTimeInterval() {
        return timeInterval;
    }

    @Override
    public long getBytes() {
        return bytes;
    }

    @Override
    public void addToState(Packet packet) {
        this.timeInterval.union(packet.getTimeInterval());
        this.bytes += packet.getBytes();
    }

    @Override
    public Abstraction getDecidingAbstraction(int childLayer, Packet packet) {
        return children.peekLast();
    }

    @Override
    public void addChildAbstraction(Abstraction childAbstraction, Packet newPacket) {
        children.add(childAbstraction);
        this.newestPacket = newPacket;
    }

    @Override
    public Packet getLastPacket(int childLayer) {
        return newestPacket;
    }

    public ConcurrentLinkedDeque<Abstraction> getChildren() {
        return children;
    }

}
