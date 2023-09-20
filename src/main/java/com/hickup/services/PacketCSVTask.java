package com.hickup.services;

import org.pcap4j.core.PcapHandle;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.TcpPacket;
import org.pcap4j.packet.UdpPacket;
import com.opencsv.CSVWriter;

import javafx.application.Platform;

import java.io.FileWriter;
import java.io.IOException;


// unused imports
// import java.text.SimpleDateFormat;
// import java.util.Date;
// import org.pcap4j.core.PcapNativeException;
// import org.pcap4j.core.PcapNetworkInterface;
// import org.pcap4j.core.Pcaps;

public class PacketCSVTask extends PacketTask {


    CSVWriter csvWriter;

    public PacketCSVTask(final String fileName, final String filter, final String networkInterfaceName, final String receiverIP) {
        super(filter, networkInterfaceName, receiverIP);
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(fileName);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        csvWriter = new CSVWriter(fileWriter);
        csvWriter.writeNext(new String[]{"Time", "Source IP", "Destination IP", "Source Port", "Destination Port", "Protocol", "Payload Length", "Fin", "Syn", "Rst", "Psh", "Ack", "Urg"});
    }

    public void onCancel(PcapHandle handle) {
        try {
            csvWriter.flush();
            csvWriter.close();
        } catch (IOException e) {
            e.printStackTrace();

            Platform.runLater(() -> {
                System.out.println("Failed");
            });
        }
        handle.close();
    }

    @Override
    public void processPacket(Packet packet, PcapHandle handle, boolean isSent, String ip) {
        // check if packet is TCP or UDP
        boolean[] flags = new boolean[6];
        int length = 0;
        String timestamp = handle.getTimestamp().toString();
        String src_port = "";
        String dst_port = "";
        String src_addr = packet.get(IpV4Packet.class).getHeader().getSrcAddr().getHostAddress();
        String dest_addr  = packet.get(IpV4Packet.class).getHeader().getDstAddr().getHostAddress();
        String protocol = "";


        if(packet.contains(TcpPacket.class)) {
            // get payload length from TCPPacket
            TcpPacket tcpPacket = packet.get(TcpPacket.class);
            if(tcpPacket.getPayload() != null) {
                length = tcpPacket.getPayload().length();
            }

            src_port = tcpPacket.getHeader().getSrcPort().valueAsString();
            dst_port = tcpPacket.getHeader().getDstPort().valueAsString();

            // set flags
            flags[0] = tcpPacket.getHeader().getFin();
            flags[1] = tcpPacket.getHeader().getSyn();
            flags[2] = tcpPacket.getHeader().getRst();
            flags[3] = tcpPacket.getHeader().getPsh();
            flags[4] = tcpPacket.getHeader().getAck();
            flags[5] = tcpPacket.getHeader().getUrg();

            protocol = "TCP";

        } else if(packet.contains(UdpPacket.class)) {
            // get payload length from UDPPacket
            UdpPacket udpPacket = packet.get(UdpPacket.class);
            length = udpPacket.getPayload().length();
            

            protocol = "UDP";
        } else {
            length = packet.length();

            protocol = "Other";
        }

        // write to csv
        csvWriter.writeNext(new String[]{
            timestamp, 
            src_addr, 
            dest_addr, 
            src_port, 
            dst_port, 
            protocol, 
            Integer.toString(length), 
            Boolean.toString(flags[0]), 
            Boolean.toString(flags[1]), 
            Boolean.toString(flags[2]), 
            Boolean.toString(flags[3]), 
            Boolean.toString(flags[4]), 
            Boolean.toString(flags[5])
        });
    }
}