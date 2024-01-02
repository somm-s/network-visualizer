package ch.cydcampus.hickup.core;

import java.net.InetAddress;
import java.net.UnknownHostException;

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
    }

    public static enum AttributeType {
        String, Integer, Double, Long, Boolean
    }

    public static String[] attributeNames;
    public static AttributeType[] attributeTypes;

    private long bytes;
    private long numSubTokens;
    private InetAddress srcIP;
    private InetAddress dstIP;
    private int srcPort;
    private int dstPort;
    private Protocol protocol;
    private String[] attributes;
    private TimeInterval timeInterval;

    public static void setAttributeNames(String[] attributeNames) {
        Packet.attributeNames = attributeNames;
    }

    public static void setAttributeTypes(AttributeType[] attributeTypes) {
        Packet.attributeTypes = attributeTypes;
    }

    public Packet() {
        this.bytes = 0;
        this.numSubTokens = 0;
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
        this.numSubTokens = numSubTokens;
        this.srcIP = InetAddress.getByName(srcIP);
        this.dstIP = InetAddress.getByName(dstIP);
        this.srcPort = srcPort;
        this.dstPort = dstPort;
        this.protocol = protocol;
        initializeAttributes();
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

    public long getNumSubTokens() {
        return numSubTokens;
    }

    public void setNumSubTokens(long numSubTokens) {
        this.numSubTokens = numSubTokens;
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

    public void addBytesFromOther(Packet packet) {
        this.bytes += packet.getBytes();
    }

    public void setContentTo(Packet other) {
        this.bytes = other.getBytes();
        this.numSubTokens = other.getNumSubTokens();
        this.srcIP = other.getSrcIP();
        this.dstIP = other.getDstIP();
        this.srcPort = other.getSrcPort();
        this.dstPort = other.getDstPort();
        this.protocol = other.getProtocol();
    }

    public String toString() {
        return "bytes: " + bytes + ", numSubTokens: " + numSubTokens + ", srcIP: " + srcIP + ", dstIP: " + dstIP + ", srcPort: " + srcPort + ", dstPort: " + dstPort + ", protocol: " + protocol;
    }

    public void incrementSubTokenCount() {
        numSubTokens++;
    }

    public void addAttribute(String attributeName, String attributeValue) {
        if(attributeNames == null) {
            throw new RuntimeException("Attribute names not set.");
        }

        for(int i = 0; i < attributeNames.length; i++) {
            if(attributeNames[i].equals(attributeName)) {
                attributes[i] = attributeValue;
                return;
            }
        }
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

}
