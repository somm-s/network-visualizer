package ch.cydcampus.hickup.pipeline;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Timestamp;

import org.pcap4j.packet.IpPacket;
import org.pcap4j.packet.TcpPacket;
import org.pcap4j.packet.UdpPacket;

import ch.cydcampus.hickup.pipeline.PacketAbstraction.Protocol;
import ch.cydcampus.hickup.pipeline.feature.Feature;
import ch.cydcampus.hickup.util.TimeInterval;

public class AbstractionFactory {

    public static final int[] TIMEOUTS = { 0, 1000, 10000, 100000, 1000000, 10000000 };

    public static Abstraction createHighOrderAbstraction(int level) {
        assert level > 0;
        HighOrderAbstraction highOrderAbstraction = new HighOrderAbstraction(level, TIMEOUTS[level]);

        // TODO add all features of this abstraction level

        return highOrderAbstraction;
    }

    /*
     * Allocate a sequential token and populate it with the data from the network packet.
     * 
     * @param packet The packet to allocate the token from. Returns null if the packet does not contain an IP packet.
     */
    public Abstraction allocateFromNetwork(org.pcap4j.packet.Packet networkPacket, Timestamp timestamp) throws UnknownHostException {
        if(!networkPacket.contains(IpPacket.class)) {
            return null;
        }

        Feature[] features = new Feature[PipelineConfig.LEVEL_0_FEATURES.length];
        InetAddress srcAddr = networkPacket.get(IpPacket.class).getHeader().getSrcAddr();
        InetAddress dstAddr  = networkPacket.get(IpPacket.class).getHeader().getDstAddr();
        Protocol protocol = Protocol.ANY;
        long bytes = networkPacket.length();
        int srcPort = 0;
        int dstPort = 0;
        long time = TimeInterval.timeToMicro(timestamp);

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

        


        return new PacketAbstraction();
    }

    public static Abstraction createPackeAbstraction() {
        PacketAbstraction packetAbstraction = new PacketAbstraction();
        return packetAbstraction;
    }

}
