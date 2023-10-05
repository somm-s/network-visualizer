package com.lockedshields;

import java.io.EOFException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapHandle.TimestampPrecision;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.Pcaps;
import org.pcap4j.packet.IpPacket;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.TcpPacket;
import org.pcap4j.packet.UdpPacket;

import com.opencsv.CSVWriter;

public class StatsLoader {

    public static void main(String[] args) throws IOException {
        
        // path to folder with pcap files:
        // String pcapFolderPath = "~/Documents/ls-remote/2022-data/BT03-CHE/pcaps/";
        String pcapFolderPath = "/home/lab/Documents/networking/hickup-net/pcaps";

        // get a list with all files in folder:
        File folder = new File(pcapFolderPath);
        File[] listOfFiles = folder.listFiles();
        System.out.println(listOfFiles.length + " " + listOfFiles[0].getAbsolutePath());

        String pcapFilePath = "kolka-220420-00002080.pcap.gz";

        // open the new csv file to write the statistics to. use opencsv library
        String csvFilePath = "stats.csv";
        File csvFile = new File(csvFilePath);
        CSVWriter writer = new CSVWriter(new FileWriter(csvFile));
        String[] columnNames = {"ID", "Relative Name", "Time", "Last Packet Time", "Packets", "Bytes", "Packets per Second", "Bytes per Second", "Average Packet Size", "IPs", "Src Ports", "Dst Ports", "Host-to-Host Packets", "Protocols"};
        writer.writeNext(columnNames);
        writer.flush();

        // iterate over all pcap files, decompress them and write the extracted statistics to the csv file
        for (int i = 0; i < listOfFiles.length; i++) {
            pcapFilePath = listOfFiles[i].getAbsolutePath();
            System.out.println(pcapFilePath);

            // decompress pcap.gz file
            String tempFileName = "temp.pcap";
            
            PcapDecompressor.decompress(pcapFilePath, tempFileName);

            // variables for csv file:
            // get the name of the pcap file
            String relativeName = listOfFiles[i].getName();
            int ID = i;
            long time = 0;
            long lastPacketTime = 0;
            long packets = 0;
            long bytes = 0;
            long packetsPerSecond = 0;
            long bytesPerSecond = 0;
            long avgPacketSize = 0;
            String IPs = ""; // all unique IPs in the pcap file, separated by a semi-colon
            String srcPorts = ""; // all unique src ports in the pcap file and their occurency, in the format "port-occurence", separated by a semi-colon
            String dstPorts = ""; // all unique dst ports in the pcap file and their occurency, in the format "port-occurence", separated by a semi-colon
            String hostToHostPackets = ""; // for each host-to-host communication in pcap, indicate the number of packets in the format "srcIP-dstIP-packets", separated by a semi-colon
            String protocols = ""; // all unique protocols in the pcap file and their occurences, in the format protocol-occurence, separated by a semi-colon

            // datastructures to accumulate data
            Set<String> uniqueIPs = new java.util.HashSet<String>();
            Map<String, Integer> uniqueSrcPorts = new java.util.HashMap<String, Integer>();
            Map<String, Integer> uniqueDstPorts = new java.util.HashMap<String, Integer>();
            Map<String, Integer> uniqueHostToHostPackets = new java.util.HashMap<String, Integer>();
            Map<String, Integer> uniqueProtocols = new java.util.HashMap<String, Integer>();

            // Open the pcap file
            PcapHandle handle;
            try {
                handle = Pcaps.openOffline(tempFileName, TimestampPrecision.NANO);
            } catch (PcapNativeException e) {
                System.out.println("Couldn't open pcap file: " + tempFileName);
                e.printStackTrace();
                return;
            }

            // iterate over all packets in the pcap file using while loop
            Packet packet;
            try {
                packet = handle.getNextPacketEx();
            } catch (PcapNativeException | TimeoutException | NotOpenException e) {
                System.out.println("Couldn't get first packet from pcap file: " + tempFileName);
                e.printStackTrace();
                continue;
            }
            
            while (packet != null) {
                // count packets
                packets++;

                // count bytes
                bytes += packet.length();

                // get the timestamp of the first packet
                if (time == 0) {
                    time = handle.getTimestamp().getTime();
                }

                // get all unique IPs
                uniqueIPs.add(packet.get(IpPacket.class).getHeader().getSrcAddr().getHostAddress());
                uniqueIPs.add(packet.get(IpPacket.class).getHeader().getDstAddr().getHostAddress());

                // get all unique ports
                if (packet.contains(TcpPacket.class)) {

                    // unique ports update (1 if not in map)
                    String srcPort = packet.get(TcpPacket.class).getHeader().getSrcPort().toString();
                    if (uniqueSrcPorts.containsKey(srcPort)) {
                        uniqueSrcPorts.put(srcPort, uniqueSrcPorts.get(srcPort) + 1);
                    } else {
                        uniqueSrcPorts.put(srcPort, 1);
                    }

                    String dstPort = packet.get(TcpPacket.class).getHeader().getDstPort().toString();
                    if (uniqueDstPorts.containsKey(dstPort)) {
                        uniqueDstPorts.put(dstPort, uniqueDstPorts.get(dstPort) + 1);
                    } else {
                        uniqueDstPorts.put(dstPort, 1);
                    }

                }

                if (packet.contains(UdpPacket.class)) {

                    // unique ports update (1 if not in map)
                    String srcPort = packet.get(UdpPacket.class).getHeader().getSrcPort().toString();
                    if (uniqueSrcPorts.containsKey(srcPort)) {
                        uniqueSrcPorts.put(srcPort, uniqueSrcPorts.get(srcPort) + 1);
                    } else {
                        uniqueSrcPorts.put(srcPort, 1);
                    }

                    String dstPort = packet.get(UdpPacket.class).getHeader().getDstPort().toString();
                    if (uniqueDstPorts.containsKey(dstPort)) {
                        uniqueDstPorts.put(dstPort, uniqueDstPorts.get(dstPort) + 1);
                    } else {
                        uniqueDstPorts.put(dstPort, 1);
                    }
                }

                // get all unique protocols using packet.get(IpPacket.class).getHeader().getProtocol().toString());
                String protocol = packet.get(IpPacket.class).getHeader().getProtocol().toString();
                if (uniqueProtocols.containsKey(protocol)) {
                    uniqueProtocols.put(protocol, uniqueProtocols.get(protocol) + 1);
                } else {
                    uniqueProtocols.put(protocol, 1);
                }

                // get all unique host-to-host connections
                String srcIP = packet.get(IpPacket.class).getHeader().getSrcAddr().getHostAddress();
                String dstIP = packet.get(IpPacket.class).getHeader().getDstAddr().getHostAddress();
                String key = srcIP + "-" + dstIP;
                if (uniqueHostToHostPackets.containsKey(key)) {
                    uniqueHostToHostPackets.put(key, uniqueHostToHostPackets.get(key) + 1);
                } else {
                    uniqueHostToHostPackets.put(key, 1);
                }

                // get the next non-corrupt packet. Set to null if no more packets in stream.
                while(true) {
                    try {
                        packet = handle.getNextPacketEx();
                    } catch (PcapNativeException | TimeoutException | NotOpenException | EOFException e) {
                        System.out.println("Finished reading packets from : " + tempFileName);
                        packet = null;
                        break;
                    } catch (IllegalArgumentException e) {
                        System.out.println("corrupt packet, skipped...");
                        continue;
                    }
                    break;
                }

            }

            // get the timestamp of the last packet
            lastPacketTime = handle.getTimestamp().getTime();

            // calculate packets per second
            packetsPerSecond = (packets * 1000000) / (lastPacketTime - time);

            // calculate bytes per second
            bytesPerSecond = (bytes * 1000000) / (lastPacketTime - time);

            // calculate average packet size
            avgPacketSize = bytes / packets;
            

            // construct strings for csv file
            IPs = String.join(";", uniqueIPs);
            
            // host-to-host
            for (Map.Entry<String, Integer> entry : uniqueHostToHostPackets.entrySet()) {
                hostToHostPackets += entry.getKey() + "-" + entry.getValue() + ";";
            }
            
            // protocols
            for (Map.Entry<String, Integer> entry : uniqueProtocols.entrySet()) {
                protocols += entry.getKey() + "-" + entry.getValue() + ";";
            }

            // srcPorts
            for (Map.Entry<String, Integer> entry : uniqueSrcPorts.entrySet()) {
                srcPorts += entry.getKey() + "-" + entry.getValue() + ";";
            }

            // dstPorts
            for (Map.Entry<String, Integer> entry : uniqueDstPorts.entrySet()) {
                dstPorts += entry.getKey() + "-" + entry.getValue() + ";";
            }

            // write data to csv file
            String[] data = {
                Integer.toString(ID), 
                relativeName, 
                Long.toString(time), 
                Long.toString(lastPacketTime), 
                Long.toString(packets), 
                Long.toString(bytes), 
                Long.toString(packetsPerSecond), 
                Long.toString(bytesPerSecond), 
                Long.toString(avgPacketSize), 
                IPs, 
                srcPorts, 
                dstPorts,
                hostToHostPackets, 
                protocols
            };

            writer.writeNext(data);

            // Close the handle
            handle.close();
            
            // delete the temporary file
            File file = new File(tempFileName);
            file.delete();

            // flush the writer
            writer.flush();
        }
    }   
}
