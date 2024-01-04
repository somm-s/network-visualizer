package ch.cydcampus.hickup.core.feature;

import ch.cydcampus.hickup.core.Packet;

public class FlowIdentifierFeature implements Feature {

    @Override
    public String getFeatureName() {
        return "flow_identifier";
    }

    @Override
    public void enrichFeature(Packet packet) {
        String src = packet.getSrcIP().getHostAddress();
        String dst = packet.getDstIP().getHostAddress();
        if(src.compareTo(dst) < 0) {
            packet.addAttribute(getFeatureName(), src + "-" + dst + "-" + packet.getSrcPort() + "-" + packet.getDstPort() + "-" + packet.getProtocol());
        } else {
            packet.addAttribute(getFeatureName(), dst + "-" + src + "-" + packet.getDstPort() + "-" + packet.getSrcPort() + "-" + packet.getProtocol());
        }
    }
    
}
