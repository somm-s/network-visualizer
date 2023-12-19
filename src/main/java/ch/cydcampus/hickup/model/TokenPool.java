package ch.cydcampus.hickup.model;

import ch.cydcampus.hickup.model.TokenState.Protocol;
import ch.cydcampus.hickup.util.TimeInterval;

import java.sql.Timestamp;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.pcap4j.packet.IpPacket;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.TcpPacket;
import org.pcap4j.packet.UdpPacket;

/*
 * Represents a pool of tokens. 
 * Can allocate and free tokens in constant time. 
 * This class is thread safe.
 */
public class TokenPool {
    private BlockingQueue<ParallelToken> parallelTokenQueue;
    private BlockingQueue<SequentialToken> sequentialTokenQueue;
    private static final TokenPool instance = new TokenPool(); // Singleton instance

    // Private constructor to enforce singleton pattern
    private TokenPool() {
        parallelTokenQueue = new LinkedBlockingQueue<>();
        sequentialTokenQueue = new LinkedBlockingQueue<>();
    }

    /*
     * Returns the singleton instance of the token pool.
     */
    public static TokenPool getPool() {
        return instance;
    }

    public void releaseParallelToken(ParallelToken token) {
        parallelTokenQueue.offer(token);
    }

    public void releaseSequentialToken(SequentialToken token) {
        sequentialTokenQueue.offer(token);
    }



    /*
     * Allocate a parallel token and populate it with the data from the network packet.
     * The token is located as a non-leaf node in the token tree.
     */
    public ParallelToken allocateParallelToken(Token packetToken, int level) {
        ParallelToken token = allocateParallelToken();
        populateToken(
            token, 
            packetToken.getTimeInterval().getStart(), 
            packetToken.getTimeInterval().getEnd(), 
            level,
            0, // empty token on creation. Will be updated later when the sub tokens are added.
            packetToken.getState().getSrcIP(),
            packetToken.getState().getDstIP(),
            packetToken.getState().getSrcPort(),
            packetToken.getState().getDstPort(),
            packetToken.getState().getProtocol());
        return token;
    }


    /*
     * Allocate a sequential token and populate it with the data from the network packet.
     * The token is located as a leaf node in the token tree.
     */
    public SequentialToken allocateSequentialToken(Token packetToken, int level) {
        SequentialToken token = allocateSequentialToken();
        populateToken(
            token, 
            packetToken.getTimeInterval().getStart(), 
            packetToken.getTimeInterval().getEnd(), 
            level,
            0, // empty token on creation. Will be updated later when the sub tokens are added.
            packetToken.getState().getSrcIP(),
            packetToken.getState().getDstIP(),
            packetToken.getState().getSrcPort(),
            packetToken.getState().getDstPort(),
            packetToken.getState().getProtocol());
        return token;
    }




    /*
     * Allocate a sequential token and populate it with the data from the network packet.
     * 
     * @param packet The packet to allocate the token from. Returns null if the packet does not contain an IP packet.
     */
    public SequentialToken allocateFromPacket(Packet packet, Timestamp timestamp) {
        if(!packet.contains(IpPacket.class)) {
            return null;
        }

        String srcAddr = packet.get(IpPacket.class).getHeader().getSrcAddr().getHostAddress();
        String dstAddr  = packet.get(IpPacket.class).getHeader().getDstAddr().getHostAddress();
        Protocol protocol = Protocol.ANY;
        int bytes = packet.length();
        int srcPort = 0;
        int dstPort = 0;

        if(packet.contains(TcpPacket.class)) {
            TcpPacket tcpPacket = packet.get(TcpPacket.class);
            srcPort = tcpPacket.getHeader().getSrcPort().valueAsInt();
            dstPort = tcpPacket.getHeader().getDstPort().valueAsInt();
            if(tcpPacket.getPayload() != null) {
                bytes = tcpPacket.getPayload().length();
            }
            protocol = Protocol.TCP;
        } else if(packet.contains(UdpPacket.class)) {
            UdpPacket udpPacket = packet.get(UdpPacket.class);
            if(udpPacket.getPayload() != null) {
                bytes = udpPacket.getPayload().length();
            }
            srcPort = udpPacket.getHeader().getSrcPort().valueAsInt();
            dstPort = udpPacket.getHeader().getDstPort().valueAsInt();
            protocol = Protocol.UDP;
        }

        SequentialToken token = allocateSequentialToken();
        populateToken(token, TimeInterval.timeToMicro(timestamp), TimeInterval.timeToMicro(timestamp), Token.PACKET_LAYER, bytes, srcAddr, dstAddr, srcPort, dstPort, protocol);
        return token;
    }

    /*
     * Allocate a sequential token and populate it with the row of a csv file.
     */
    public SequentialToken allocateFromString(String row) throws Exception {

        String[] split = row.split(",");
        Protocol protocol = Protocol.fromInt(Integer.parseInt(split[0]));
        int bytes = Integer.parseInt(split[1]);
        TimeInterval timeInterval = null;
        timeInterval = new TimeInterval(split[2], split[2]);

        String srcAddr = split[3];
        String dstAddr = split[4];

        int srcPort = 0;
        int dstPort = 0;

        if(split.length >= 7) {
            srcPort = Integer.parseInt(split[5]);
            dstPort = Integer.parseInt(split[6]);
        }
        SequentialToken token = allocateSequentialToken();
        populateToken(token, timeInterval.getStart(), timeInterval.getEnd(), Token.PACKET_LAYER, bytes, srcAddr, dstAddr, srcPort, dstPort, protocol);
        return token;
    }

    /*
     * Allocate a sequential (packet) token with all fields
     */
    public SequentialToken allocateFromFields(Protocol protocol, int bytes, TimeInterval timeInterval, String srcAddr, String dstAddr, int srcPort, int dstPort) {
        SequentialToken token = allocateSequentialToken();
        populateToken(token, timeInterval.getStart(), timeInterval.getEnd(), Token.PACKET_LAYER, bytes, srcAddr, dstAddr, srcPort, dstPort, protocol);
        return token;
    }

    private SequentialToken allocateSequentialToken() {
        SequentialToken token = sequentialTokenQueue.poll();

        if (token == null) {
            TokenState tokenState = new TokenState();
            TimeInterval tokenTimeInterval = new TimeInterval();
            token = new SequentialToken(tokenState, tokenTimeInterval, 0);
        }
        return token;
    }

    ParallelToken allocateParallelToken() {
        ParallelToken token = parallelTokenQueue.poll();
        if (token == null) {
            TokenState tokenState = new TokenState();
            TimeInterval tokenTimeInterval = new TimeInterval();
            token = new ParallelToken(tokenState, tokenTimeInterval, 0);
        }
        return token;
    }

    private void populateToken(Token token, long start, long end, int level, long bytes, String srcAddr, String dstAddr, int srcPort, int dstPort, Protocol protocol) {
        token.setLevel(level);
        token.getState().setBytes(bytes);
        token.getState().setNumSubTokens(0);
        token.getState().setSrcIP(srcAddr);
        token.getState().setDstIP(dstAddr);
        token.getState().setSrcPort(srcPort);
        token.getState().setDstPort(dstPort);
        token.getState().setProtocol(protocol);
        token.getTimeInterval().updateTimeInterval(start, end);
    }
}
