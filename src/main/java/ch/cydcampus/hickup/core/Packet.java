package ch.cydcampus.hickup.core;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.Map;

import ch.cydcampus.hickup.core.abstraction.Abstraction;
import ch.cydcampus.hickup.util.TimeInterval;

public class Packet implements Abstraction {
    
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

    public static enum AttributeType {
        String, Integer, Double, Long, Boolean
    }

    public static Map<String, Integer> attributeIndices;
    public static String[] attributeNames;
    public static AttributeType[] attributeTypes;

    private long bytes;
    private InetAddress srcIP;
    private InetAddress dstIP;
    private int srcPort;
    private int dstPort;
    private Protocol protocol;
    private String[] attributes;
    private TimeInterval timeInterval;

    /*
     * Static attribute mapping initialized by the configuration loader.
     */
    public static void setAttributeNames(String[] attributeNames) {
        Packet.attributeNames = attributeNames;
    }

    public static void setAttributeTypes(AttributeType[] attributeTypes) {
        Packet.attributeTypes = attributeTypes;
    }

    public static void setAttributeIndices(Map<String, Integer> attributeIndices) {
        Packet.attributeIndices = attributeIndices;
    }

    /*
     * Creates a new packet with default values.
     */
    public Packet() {
        this.bytes = 0;
        this.srcPort = 0;
        this.dstPort = 0;
        this.protocol = Protocol.ANY;
        this.timeInterval = new TimeInterval();
        initializeAttributes();
    }

    /*
     * Creates a new token state with values. Should only be invoked by packet tokens.
     */
    public Packet(long bytes, long numSubTokens, String srcIP, 
        String dstIP, int srcPort, int dstPort, Protocol protocol) throws UnknownHostException {
        
        this.bytes = bytes;
        this.srcIP = InetAddress.getByName(srcIP);
        this.dstIP = InetAddress.getByName(dstIP);
        this.srcPort = srcPort;
        this.dstPort = dstPort;
        this.protocol = protocol;
        initializeAttributes();
    }

    private void initializeAttributes() {
        if(attributeNames == null) {
            return;
        }

        attributes = new String[attributeNames.length];
        for(int i = 0; i < attributes.length; i++) {
            attributes[i] = "";
        }
    }

    private String getTimeString() {
        Instant startInstant = TimeInterval.microToInstant(timeInterval.getStart());
        String timeString = TimeInterval.timeFormatter.format(startInstant);
        return timeString;
    }

    public TimeInterval getTimeInterval() {
        return timeInterval;
    }

    public long getBytes() {
        return bytes;
    }

    public void setBytes(long bytes) {
        this.bytes = bytes;
    }

    public InetAddress getSrcIP() {
        return srcIP;
    }

    public void setSrcIP(String srcIP) throws UnknownHostException {
        this.srcIP = InetAddress.getByName(srcIP);
    }

    public void setSrcIP(InetAddress srcIP) {
        this.srcIP = srcIP;
    }

    public InetAddress getDstIP() {
        return dstIP;
    }

    public void setDstIP(String dstIP) throws UnknownHostException {
        this.dstIP = InetAddress.getByName(dstIP);
    }

    public void setDstIP(InetAddress dstIP) {
        this.dstIP = dstIP;
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

    public Protocol getProtocol() {
        return protocol;
    }

    public void setProtocol(Protocol protocol) {
        if(protocol == null) {
            this.protocol = Protocol.ANY;
        } else {
            this.protocol = protocol;
        }
    }

    /*
     * Returns a string that uniquely identifies the bidirectional flow based on its 5-tuple.
     */
    public String getBidirectionalFlowIdentifier() {
        if(srcIP.getHostAddress().compareTo(dstIP.getHostAddress()) < 0) {
            return srcIP.getHostAddress() + "-" + dstIP.getHostAddress() + "-" + srcPort + "-" + dstPort + "-" + protocol;
        } else {
            return dstIP.getHostAddress() + "-" + srcIP.getHostAddress() + "-" + dstPort + "-" + srcPort + "-" + protocol;
        }
    }

    public String getHostToHostIdentifier() {
        if(srcIP.getHostAddress().compareTo(dstIP.getHostAddress()) < 0) {
            return srcIP.getHostAddress() + "-" + dstIP.getHostAddress();
        } else {
            return dstIP.getHostAddress() + "-" + srcIP.getHostAddress();
        }
    }

    public int getDirection() {
        if(srcIP.getHostAddress().compareTo(dstIP.getHostAddress()) < 0) {
            return 1;
        } else {
            return -1;
        }
    }

    public void addBytesFromOther(Packet packet) {
        this.bytes += packet.getBytes();
    }

    public void setContentTo(Packet other) {
        this.bytes = other.getBytes();
        this.srcIP = other.getSrcIP();
        this.dstIP = other.getDstIP();
        this.srcPort = other.getSrcPort();
        this.dstPort = other.getDstPort();
        this.protocol = other.getProtocol();
    }

    
    public void addAttribute(String attributeName, String attributeValue) {
        if(attributeIndices == null) {
            throw new RuntimeException("Attributes uninitialized.");
        }
        
        int index = attributeIndices.get(attributeName);
        attributes[index] = attributeValue;
    }
    
    public String toString() {
        return "bytes: " + bytes + ", srcIP: " + srcIP + ", dstIP: " + dstIP + ", srcPort: " + srcPort + ", dstPort: " + dstPort + ", protocol: " + protocol + " time: " + getTimeString();
    }

    public String getAttributeString(String attributeName) {
        if(attributeIndices.containsKey(attributeName)) {
            return attributes[attributeIndices.get(attributeName)];
        }

        switch(attributeName) {
            case "bytes":
                return Long.toString(bytes);
            case "time":
                return getTimeString();
            case "srcIP":
                return srcIP.getHostAddress();
            case "dstIP":
                return dstIP.getHostAddress();
            case "srcPort":
                return Integer.toString(srcPort);
            case "dstPort":
                return Integer.toString(dstPort);
            case "protocol":
                return Protocol.toString(protocol);
            default:
                throw new RuntimeException("Unknown attribute: " + attributeName);
        } 
    }

    @Override
    public boolean isRoot() {
        return false;
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public int getLayer() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void addToState(Packet packet) {
        throw new RuntimeException("Cannot add packet to packet.");
    }

    @Override
    public Packet getLastPacket(int childLayer) {
        throw new RuntimeException("Cannot get last packet from packet.");
    }

    @Override
    public Abstraction getDecidingAbstraction(int childLayer, Packet packet) {
        throw new RuntimeException("Cannot get deciding abstraction from packet.");
    }

    @Override
    public void addChildAbstraction(Abstraction childAbstraction, Packet newPacket) {
        throw new RuntimeException("Cannot add child abstraction to packet.");
    }

}
