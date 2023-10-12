package com.hickup.points;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

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

    // serializes to string with toString method of dynamic type
    public abstract String toString();

    // deserializes from string
    public static IPPoint fromString(String s) {
        IPPoint res = null;

        // use deserialization of dynamic type. String in csv style
        String[] parts = s.split(",");
        if(parts[0].equals("0")) {
            res = TCPPoint.fromString(s);
        } else if(parts[0].equals("1")) {
            res = UDPPoint.fromString(s);
        } else if(parts[0].equals("2")) {
            res = AnyPoint.fromString(s);
        } else {
            throw new IllegalArgumentException("Invalid string format");
        }
        return res;
    }

    public static java.sql.Timestamp timeFromString(String timeString) {
        // Create a DateTimeFormatter with nanoseconds pattern
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS")
                .withZone(TimeZone.getTimeZone("UTC").toZoneId());

        // Parse the string to Instant
        Instant instant = Instant.from(formatter.parse(timeString));

        // Convert Instant to Timestamp
        java.sql.Timestamp timestamp = java.sql.Timestamp.from(instant);

        return timestamp;
    }

    public static String timeToString(java.sql.Timestamp timestamp) {
        // Convert Timestamp to Instant
        Instant instant = timestamp.toInstant();

        // Create a DateTimeFormatter with nanoseconds pattern
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS").withZone(TimeZone.getTimeZone("UTC").toZoneId());

        // Format the timestamp with nanoseconds
        String formattedTimestamp = formatter.format(instant);

        return formattedTimestamp;
    }


    // static function to read ip points from a file. returns array of ippoints.
    public static IPPoint[] readFromFile(String path) {
        List<IPPoint> res = new LinkedList<IPPoint>();
        try {
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(path));

            String line = reader.readLine();
            
            while(line != null && !line.equals("")) {
                res.add(IPPoint.fromString(line));
                line = reader.readLine();
            }

            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        IPPoint[] res_arr = new IPPoint[res.size()];
        return res.toArray(res_arr);
    }

    public static IPPoint[] readFromFiles(String folderPath, String filterIP) throws IOException {
        File folder = new File(folderPath);
        File[] listOfFiles = folder.listFiles();

        // sort list to have the files in the correct order
        java.util.Arrays.sort(listOfFiles);

        // List that is returned
        List<IPPoint> res = new LinkedList<IPPoint>();

        // go over all files
        for(int i = 0; i < listOfFiles.length; i++) {
            System.out.println("Reading file " + listOfFiles[i].getName());
            int numPackets = 0;
            BufferedReader reader = new BufferedReader(new FileReader(listOfFiles[i]));

            String line = reader.readLine();
            
            while(line != null && !line.equals("")) {
                // apply filter:
                IPPoint p = IPPoint.fromString(line);
                if(p.dstIp.equals(filterIP) || p.srcIp.equals(filterIP)) {
                    res.add(p);
                    numPackets++;
                }
                line = reader.readLine();
            }
            System.out.println("Read " + numPackets + " Packets");
            reader.close();
        }

        IPPoint[] res_arr = new IPPoint[res.size()];
        return res.toArray(res_arr);
    }
}