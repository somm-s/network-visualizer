package ch.cydcampus.hickup.core.abstraction;

import java.util.Collection;
import java.util.Map;

import ch.cydcampus.hickup.core.Packet;
import ch.cydcampus.hickup.util.TimeInterval;

public interface Abstraction {
    
    public boolean isRoot();
    public boolean isLeaf();
    public int getLayer();
    public TimeInterval getTimeInterval();
    public long getBytes();

    /*
     * Returns the last packet that was added to the abstraction. If corresponding node has multiple nodes as children, select
     * based on the childLayer parameter.
     */
    public Packet getLastPacket(int childLayer);

    /*
     * Adds effect of packet to abstraction layer.
     */
    public void addToState(Packet packet);

    /*
     * Returns the deciding abstraction from the next layer. Abstractions where the corresponding node has multiple
     * children select the child node based on the childLayer parameter.
     */
    public Abstraction getDecidingAbstraction(int childLayer, Packet packet);

    /*
     * Creates a child abstraction with the given packet. It assumes the effect of the packet has already been added to the state.
     */
    public void addChildAbstraction(Abstraction childAbstraction, Packet newPacket);

}
