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

    /*
     * Allocate a parallel token from the pool.
     */
    public ParallelToken allocateParallelToken(TokenState state, TimeInterval timeInterval, int level) {
        ParallelToken token = parallelTokenQueue.poll();
        if (token == null) {
            token = new ParallelToken(state, timeInterval, level);
        } else {
            token.getState().setContentTo(state);
            token.getTimeInterval().setContentTo(timeInterval);
            token.setLevel(level);
        }

        return token;
    }

    /*
     * Allocate a sequential token from the pool.
     */    
    public SequentialToken allocateSequentialToken(TokenState state, TimeInterval timeInterval, int level) {
        SequentialToken token = sequentialTokenQueue.poll();
        if (token == null) {
            token = new SequentialToken(state, timeInterval, level);
        } else {
            token.getState().setContentTo(state);
            token.getTimeInterval().setContentTo(timeInterval);
            token.setLevel(level);
        }


        return token;
    }

    public void releaseParallelToken(ParallelToken token) {
        parallelTokenQueue.offer(token);
    }

    public void releaseSequentialToken(SequentialToken token) {
        sequentialTokenQueue.offer(token);
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

        TimeInterval timeInterval = new TimeInterval(timestamp, timestamp);

        String srcAddr = packet.get(IpPacket.class).getHeader().getSrcAddr().getHostAddress();
        String dstAddr  = packet.get(IpPacket.class).getHeader().getDstAddr().getHostAddress();
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
        } else if(packet.contains(UdpPacket.class)) {
            UdpPacket udpPacket = packet.get(UdpPacket.class);
            if(udpPacket.getPayload() != null) {
                bytes = udpPacket.getPayload().length();
            }
            srcPort = udpPacket.getHeader().getSrcPort().valueAsInt();
            dstPort = udpPacket.getHeader().getDstPort().valueAsInt();
        }

        TokenState state = new TokenState(bytes, 0, srcAddr, dstAddr, srcPort, dstPort, Protocol.TCP);
        SequentialToken token = allocateSequentialToken(state, timeInterval, Token.PACKET_LAYER);
        return token;
    }
}
