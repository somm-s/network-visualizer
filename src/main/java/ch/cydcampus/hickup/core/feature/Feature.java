package ch.cydcampus.hickup.core.feature;

import ch.cydcampus.hickup.core.Packet;

public interface Feature {
    
    public String getFeatureName();

    public void enrichFeature(Packet packet);
    
}
