package ch.cydcampus.hickup.core.feature;

import ch.cydcampus.hickup.core.Packet;

public class HostPairIdentifierFeature implements Feature {

    @Override
    public String getFeatureName() {
        return "host_pair_identifier";
    }

    @Override
    public void enrichFeature(Packet packet) {
        String src = packet.getSrcIP().getHostAddress();
        String dst = packet.getDstIP().getHostAddress();
        if(src.compareTo(dst) < 0) {
            packet.addAttribute(getFeatureName(), src + "-" + dst);
        } else {
            packet.addAttribute(getFeatureName(), dst + "-" + src);
        }
    }
    
}
