package ch.cydcampus.hickup.core;


import ch.cydcampus.hickup.core.Packet.Protocol;
import ch.cydcampus.hickup.util.TimeInterval;

import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.pcap4j.packet.IpPacket;
import org.pcap4j.packet.TcpPacket;
import org.pcap4j.packet.UdpPacket;

/*
 * Represents a pool of tokens. 
 * Can allocate and free tokens in constant time. 
 * This class is thread safe.
 */
public class PacketPool {
    private BlockingQueue<Packet> packetQueue;
    private static final PacketPool instance = new PacketPool(); // Singleton instance

    // Private constructor to enforce singleton pattern
    private PacketPool() {
        packetQueue = new LinkedBlockingQueue<>();
    }

    /*
     * Returns the singleton instance of the token pool.
     */
    public static PacketPool getPool() {
        return instance;
    }

    public void releasePacket(Packet packet) {
        packetQueue.offer(packet);
    }


    /*
     * Allocate a sequential token and populate it with the data from the network packet.
     * 
     * @param packet The packet to allocate the token from. Returns null if the packet does not contain an IP packet.
     */
    public Packet allocateFromNetwork(org.pcap4j.packet.Packet networkPacket, Timestamp timestamp) throws UnknownHostException {
        if(!networkPacket.contains(IpPacket.class)) {
            return null;
        }

        String srcAddr = networkPacket.get(IpPacket.class).getHeader().getSrcAddr().getHostAddress();
        String dstAddr  = networkPacket.get(IpPacket.class).getHeader().getDstAddr().getHostAddress();
        Protocol protocol = Protocol.ANY;
        int bytes = networkPacket.length();
        int srcPort = 0;
        int dstPort = 0;

        if(networkPacket.contains(TcpPacket.class)) {
            TcpPacket tcpPacket = networkPacket.get(TcpPacket.class);
            srcPort = tcpPacket.getHeader().getSrcPort().valueAsInt();
            dstPort = tcpPacket.getHeader().getDstPort().valueAsInt();
            if(tcpPacket.getPayload() != null) {
                bytes = tcpPacket.getPayload().length();
            }
            protocol = Protocol.TCP;
        } else if(networkPacket.contains(UdpPacket.class)) {
            UdpPacket udpPacket = networkPacket.get(UdpPacket.class);
            if(udpPacket.getPayload() != null) {
                bytes = udpPacket.getPayload().length();
            }
            srcPort = udpPacket.getHeader().getSrcPort().valueAsInt();
            dstPort = udpPacket.getHeader().getDstPort().valueAsInt();
            protocol = Protocol.UDP;
        }

        Packet packet = allocatePacket();
        populatePacket(packet, TimeInterval.timeToMicro(timestamp), TimeInterval.timeToMicro(timestamp), bytes, srcAddr, dstAddr, srcPort, dstPort, protocol);
        return packet;
    }

    /*
     * Allocate a sequential token and populate it with the row of a csv file.
     */
    public Packet allocateFromString(String row) throws Exception {

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
        Packet packet = allocatePacket();
        populatePacket(packet, timeInterval.getStart(), timeInterval.getEnd(), bytes, srcAddr, dstAddr, srcPort, dstPort, protocol);
        return packet;
    }

    /*
     * Allocate a sequential (packet) token with all fields
     */
    public Packet allocateFromFields(Protocol protocol, int bytes, TimeInterval timeInterval, String srcAddr, String dstAddr, int srcPort, int dstPort) throws UnknownHostException {
        Packet packet = allocatePacket();
        populatePacket(packet, timeInterval.getStart(), timeInterval.getEnd(), bytes, srcAddr, dstAddr, srcPort, dstPort, protocol);
        return packet;
    }

    private Packet allocatePacket() {
        Packet packet = packetQueue.poll();

        if (packet == null) {
            packet = new Packet();
        }
        return packet;
    }

    private void populatePacket(Packet packet, long start, long end, long bytes, String srcAddr, String dstAddr, int srcPort, int dstPort, Protocol protocol) throws UnknownHostException {
        packet.setBytes(bytes);
        packet.setSrcIP(srcAddr);
        packet.setDstIP(dstAddr);
        packet.setSrcPort(srcPort);
        packet.setDstPort(dstPort);
        packet.setProtocol(protocol);
        packet.getTimeInterval().updateTimeInterval(start, end);
    }
}
