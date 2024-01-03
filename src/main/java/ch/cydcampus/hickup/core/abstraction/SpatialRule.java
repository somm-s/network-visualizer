package ch.cydcampus.hickup.core.abstraction;

import ch.cydcampus.hickup.core.Packet;

/*
 * Rule that can be applied to get a unique identifier for a packet.
 * This identifier is used to multiplex the data flow. It can either be multiplexed on an attribute or on a field of the packet. 
 */
public class SpatialRule {

    private String attributeName;

    public SpatialRule(String attributeName) {
        this.attributeName = attributeName;
    }

    public String getIdentifier(Packet packet) {
        return packet.getAttributeString(attributeName);
    }

    public String getAttributeName() {
        return attributeName;
    }

}
