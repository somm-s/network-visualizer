package ch.cydcampus.hickup.core.abstraction;

import ch.cydcampus.hickup.core.Packet;

public class TemporalRule {

    private long timeout;
    private boolean isBidirectional;
    
    public TemporalRule(long timeout, boolean isBidirectional) {
        this.timeout = timeout;
        this.isBidirectional = isBidirectional;
    }

    public long getTimeout() {
        return timeout;
    }

    public boolean isBidirectional() {
        return isBidirectional;
    }

    public boolean belongsTo(Packet packet, Packet lastPacket) {
        if(lastPacket == null) {
            return true;
        }

        if(lastPacket.getTimeInterval().getDifference(packet.getTimeInterval()) > timeout) {
            return false;
        }

        if(!isBidirectional) {
            return packet.getDirection() == lastPacket.getDirection();
        }

        //TODO: support for higher level features --> extract features hierarchically???
        
        return true;

    }

}
