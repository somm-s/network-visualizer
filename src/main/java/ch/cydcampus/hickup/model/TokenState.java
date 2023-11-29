package ch.cydcampus.hickup.model;

/*
 * Represents the state of a token.
 * Can be subclassed to extend the state with more information.
 */
public class TokenState {

    public static enum Protocol {
        TCP, UDP, ANY
    }

    private long bytes;
    private long numSubTokens;
    private String srcIP;
    private String dstIP;
    private int srcPort;
    private int dstPort;
    private Protocol protocol;
    
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

    public long getNumSubTokens() {
        return numSubTokens;
    }

    public String getSrcIP() {
        return srcIP;
    }

    public String getDstIP() {
        return dstIP;
    }

    public int getSrcPort() {
        return srcPort;
    }

    public int getDstPort() {
        return dstPort;
    }

    public Protocol getProtocol() {
        return protocol;
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

    public void addSubTokenState(TokenState subTokenState) {
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
