package ch.cydcampus.hickup.core.abstraction;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import ch.cydcampus.hickup.core.Packet;
import ch.cydcampus.hickup.util.TimeInterval;

/*
 * Either predefined ranges or split on attribute (hashmap).
 */
public class SpatialAbstraction implements Abstraction {

    private int layer;
    private SpatialRule rule;
    private HashMap<Integer, ConcurrentHashMap<String, Abstraction>> children;
    private HashMap<Integer, Packet> newestPackets;
    private volatile long bytes;
    private TimeInterval timeInterval;


    public SpatialAbstraction(int layer, int[] childLayers, SpatialRule rule) {
        int numChildren = childLayers.length;
        this.layer = layer;
        this.rule = rule;
        this.children = new HashMap<>();
        this.newestPackets = new HashMap<>();
        for(int i = 0; i < numChildren; i++) {
            children.put(childLayers[i], new ConcurrentHashMap<String, Abstraction>());
        }
        this.timeInterval = new TimeInterval();
        this.bytes = 0;
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
        this.timeInterval.addInterval(packet.getTimeInterval());
        this.bytes += packet.getBytes();
    }

    @Override
    public Abstraction getDecidingAbstraction(int childLayer, Packet packet) {
        String identifier = rule.getIdentifier(packet);
        if(!children.get(childLayer).containsKey(identifier)) {
            return null;
        } else{
            return children.get(childLayer).get(identifier);
        }
    }

    @Override
    public void addChildAbstraction(Abstraction childAbstraction, Packet packet) {
        int childLayer = childAbstraction.getLayer();
        String identifier = rule.getIdentifier(packet);
        children.get(childLayer).put(identifier, childAbstraction);
        newestPackets.put(childLayer, packet);
    }

    @Override
    public Packet getLastPacket(int childLayer) {
        return newestPackets.get(childLayer);
    }

    public HashMap<Integer, ConcurrentHashMap<String, Abstraction>> getChildren() {
        return children;
    }

    public String toString() {
        return "SpatialAbstraction(layer=" + layer  + " newestPackets=" + newestPackets + ", bytes=" + bytes + ", timeInterval=" + timeInterval + ")";
    }

    public StringBuilder deepToString(StringBuilder sb) {
        for(int childLayer : children.keySet()) {
            // sb.append("\n\n");
            for(String identifier : children.get(childLayer).keySet()) {
                sb.append("Layer: " + childLayer + " Identifier: " + identifier + "\n");
                for(int i = 0; i < layer; i++) {
                    sb.append("  ");
                }
                children.get(childLayer).get(identifier).deepToString(sb);
            }
        }
        return sb;
    }

}
