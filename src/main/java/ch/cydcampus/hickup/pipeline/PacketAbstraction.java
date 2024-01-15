package ch.cydcampus.hickup.pipeline;

import java.util.List;

import ch.cydcampus.hickup.pipeline.feature.Feature;

public class PacketAbstraction implements Abstraction {

    public static enum Protocol {
        TCP, UDP, ANY;
        public static Protocol fromInt(int parseInt) {
            switch (parseInt) {
                case 0:
                    return TCP;
                case 1:
                    return UDP;
                default:
                    return ANY;
            }
        }

        public static String toString(Protocol protocol) {
            switch (protocol) {
                case TCP:
                    return "TCP";
                case UDP:
                    return "UDP";
                default:
                    return "ANY";
            }
        }
    }

    @Override
    public long getLastUpdateTime() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getLastUpdateTime'");
    }

    @Override
    public int getLevel() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getLevel'");
    }

    @Override
    public List<Abstraction> getChildren() {
        throw new UnsupportedOperationException("Unimplemented method 'getChildren'");
    }

    @Override
    public void addChild(Abstraction abstraction) {
        throw new UnsupportedOperationException("Unimplemented method 'addChild'");
    }

    @Override
    public Abstraction getActiveAbstraction() {
        throw new UnsupportedOperationException("Unimplemented method 'getActiveAbstraction'");
    }

    @Override
    public Abstraction getNext() {
        throw new UnsupportedOperationException("Unimplemented method 'getNext'");
    }

    @Override
    public Abstraction getPrev() {
        throw new UnsupportedOperationException("Unimplemented method 'getPrev'");
    }

    @Override
    public void setNext(Abstraction abstraction) {
        throw new UnsupportedOperationException("Unimplemented method 'setNext'");
    }

    @Override
    public void setPrev(Abstraction abstraction) {
        throw new UnsupportedOperationException("Unimplemented method 'setPrev'");
    }

    @Override
    public void addFeatures(Feature[] feature) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addFeature'");
    }

    @Override
    public Feature[] getFeatures() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getFeatures'");
    }
    
}
