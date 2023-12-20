package ch.cydcampus.hickup.model;

/*
 * Represents the state of a token.
 * Can be subclassed to extend the state with more information.
 */
public class TokenState {

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

    private volatile long bytes;
    private long numSubTokens;
    private String srcIP;
    private String dstIP;
    private int srcPort;
    private int dstPort;
    private Protocol protocol;


    public TokenState() {
        this.bytes = 0;
        this.numSubTokens = 0;
        this.srcIP = "";
        this.dstIP = "";
        this.srcPort = 0;
        this.dstPort = 0;
        this.protocol = Protocol.ANY;
    }

    /*
     * Creates a new token state with values. Should only be invoked by packet tokens.
     */
    public TokenState(long bytes, long numSubTokens, String srcIP, 
        String dstIP, int srcPort, int dstPort, Protocol protocol) {
        
        this.bytes = bytes;
        this.numSubTokens = numSubTokens;
        this.srcIP = srcIP;
        this.dstIP = dstIP;
        this.srcPort = srcPort;
        this.dstPort = dstPort;
        this.protocol = protocol;
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

    public String getSrcIP() {
        return srcIP;
    }

    public void setSrcIP(String srcIP) {
        this.srcIP = srcIP;
    }

    public String getDstIP() {
        return dstIP;
    }

    public void setDstIP(String dstIP) {
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
        if(srcIP.compareTo(dstIP) < 0) {
            return srcIP + "-" + dstIP + "-" + srcPort + "-" + dstPort + "-" + protocol;
        } else {
            return dstIP + "-" + srcIP + "-" + dstPort + "-" + srcPort + "-" + protocol;
        }
    }

    public String getHostToHostIdentifier() {
        if(srcIP.compareTo(dstIP) < 0) {
            return srcIP + "-" + dstIP;
        } else {
            return dstIP + "-" + srcIP;
        }
    }

    public void addBytesFromOther(TokenState subTokenState) {
        this.bytes += subTokenState.getBytes();
    }

    public void setContentTo(TokenState other) {
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
}
