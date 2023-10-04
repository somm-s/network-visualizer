package com.hickup.points;

import org.pcap4j.packet.IpPacket;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.TcpPacket;
import org.pcap4j.packet.UdpPacket;

import javafx.scene.canvas.GraphicsContext;

public abstract class IPPoint {
    public int packetSize;
    public java.sql.Timestamp time;
    public String srcIp;
    public String dstIp;
    
    public IPPoint(int packetSize, java.sql.Timestamp time, String srcIp, String dstIp) {
        this.packetSize = packetSize;
        this.time = time;
        this.srcIp = srcIp;
        this.dstIp = dstIp;
    }

    public void draw(GraphicsContext gc, double x, double y, double width, double height) {
        gc.fillOval(x - width / 2, y - height / 2, width, height);
    }

    public String toString() {
        return "val: " + packetSize + " time: " + time;
    }

    public static IPPoint parsePacket(Packet packet, java.sql.Timestamp time) {

        IPPoint res = null;

        // check for specific network layer protocol
        if(!packet.contains(IpPacket.class)) {
            return null;
        }

        String src_addr = packet.get(IpPacket.class).getHeader().getSrcAddr().getHostAddress();
        String dest_addr  = packet.get(IpPacket.class).getHeader().getDstAddr().getHostAddress();

        // check for specific transport layer protocol
        if(packet.contains(TcpPacket.class)) {
            boolean[] flags = new boolean[6];
            int length = 0;
            TcpPacket tcpPacket = packet.get(TcpPacket.class);
            if(tcpPacket.getPayload() != null) {
                length = tcpPacket.getPayload().length();
            }
            int src_port = tcpPacket.getHeader().getSrcPort().valueAsInt();
            int dst_port = tcpPacket.getHeader().getDstPort().valueAsInt();
            TCPPoint tcpPoint = new TCPPoint(length, time, src_addr, dest_addr, src_port, dst_port);

            // set flags
            flags[0] = tcpPacket.getHeader().getFin();
            flags[1] = tcpPacket.getHeader().getSyn();
            flags[2] = tcpPacket.getHeader().getRst();
            flags[3] = tcpPacket.getHeader().getPsh();
            flags[4] = tcpPacket.getHeader().getAck();
            flags[5] = tcpPacket.getHeader().getUrg();
            tcpPoint.setFlags(flags);

            res = tcpPoint;

        } else if(packet.contains(UdpPacket.class)) {

            int length = 0;
            UdpPacket udpPacket = packet.get(UdpPacket.class);
            if(udpPacket.getPayload() != null) {
                length = udpPacket.getPayload().length();
            }
            int src_port = udpPacket.getHeader().getSrcPort().valueAsInt();
            int dst_port = udpPacket.getHeader().getDstPort().valueAsInt();
            UDPPoint udpPoint = new UDPPoint(length, time, src_addr, dest_addr, src_port, dst_port);
            res = udpPoint;
        } else {
            int length = packet.length();
            AnyPoint otherPoint = new AnyPoint(length, time, src_addr, dest_addr);
            res = otherPoint;
        }

        return res;
    }
}


// class Data {
//     double val;
//     java.sql.Timestamp time;
//     boolean isSent;
//     String ip;

//     Data(double val, java.sql.Timestamp time, boolean isSent, String ip) {
//         this.val = val;
//         this.time = time;
//         this.isSent = isSent;
//         this.ip = ip;
//     }

//     Data(double val, java.sql.Timestamp time) {
//         this.val = val;
//         this.time = time;
//         this.isSent = false;
//     }


// }