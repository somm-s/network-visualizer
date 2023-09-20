package com.hickup;

import org.pcap4j.core.BpfProgram;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.Pcaps;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.TcpPacket;
import org.pcap4j.packet.UdpPacket;

import com.hickup.points.AnyPoint;
import com.hickup.points.IPPoint;
import com.hickup.points.TCPPoint;
import com.hickup.points.UDPPoint;

import java.util.concurrent.TimeoutException;
import java.io.EOFException;
import javafx.application.Platform;
import javafx.concurrent.Task;

// unused imports
// import java.text.SimpleDateFormat;
// import java.util.Date;
// import org.pcap4j.core.PcapNativeException;
// import org.pcap4j.core.PcapNetworkInterface;
// import org.pcap4j.core.Pcaps;

public class PacketCaptureTask extends Task<Void> {

    final String filter;
    final String networkInterfaceName;
    final String receiverIP; // 192.168.200.29
    private PacketCaptureService service;

    public PacketCaptureTask(final String filter, final String networkInterfaceName, final String receiverIP, PacketCaptureService service) {
        this.filter = filter;
        this.networkInterfaceName = networkInterfaceName;
        this.receiverIP = receiverIP;
        this.service = service;
    }
    
    @Override protected Void call() throws Exception {

        PcapNetworkInterface networkInterface = null;;

        // Set up the network interface for packet capture
        try {
            networkInterface = Pcaps.getDevByName(networkInterfaceName); // wlp0s20f3 vboxnet0
        } catch (PcapNativeException e) {
            e.printStackTrace();
            System.exit(1);
        }
        BpfProgram.BpfCompileMode mode = BpfProgram.BpfCompileMode.OPTIMIZE;
        PcapHandle handle = networkInterface.openLive(65536, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, 10);
        handle.setFilter(filter, mode);

        // Set up the packet listener
        while (true) {
            if (isCancelled()) break;
            try {
                Packet packet = null;
                try {
                    packet = handle.getNextPacketEx();
                } catch (EOFException | TimeoutException e) {
                    e.printStackTrace();
                }
                if (packet != null) {

                    // if not IP packet, ignore
                    if (!packet.contains(IpV4Packet.class)) continue;
                    
                    // extract src and dest IP addresses
                    String src_addr = packet.get(IpV4Packet.class).getHeader().getSrcAddr().getHostAddress();
                    String dest_addr  = packet.get(IpV4Packet.class).getHeader().getDstAddr().getHostAddress();
                    String ip = null;

                    // if receiverIP not in src or dest, ignore. Set isSent according to whether src or dest is receiverIP
                    boolean isSent = false;
                    if (src_addr.equals(receiverIP)) {
                        isSent = true;
                        ip = dest_addr;
                    } else if (dest_addr.equals(receiverIP)) {
                        isSent = false;
                        ip = src_addr;
                    } else {
                        continue;
                    }

                    final IPPoint point = createPointFromPacket(packet, handle.getTimestamp(), isSent, ip);

                    Platform.runLater(new Runnable() { 
                        @Override public void run() {
                            service.setCapturedData(point);
                        }
                    });
                }
            } catch (PcapNativeException | NotOpenException e) {
                e.printStackTrace();
            }
        }
        handle.close();
        return null;
    }

    // function to create TCPPoint or UDPPoint from packet, return as IPPoint
    private IPPoint createPointFromPacket(Packet packet, java.sql.Timestamp time, boolean isSent, String ip) {
        IPPoint point = null;

        // check if packet is TCP or UDP and create appropriate point
        if(packet.contains(TcpPacket.class)) {
            // get payload length from TCPPacket
            int length = 0;
            TcpPacket tcpPacket = packet.get(TcpPacket.class);
            if(tcpPacket.getPayload() != null) {
                length = tcpPacket.getPayload().length();
            }
            TCPPoint tcpPoint = new TCPPoint(length, time, isSent, ip);

            // set flags
            boolean[] flags = new boolean[6];
            flags[0] = tcpPacket.getHeader().getFin();
            flags[1] = tcpPacket.getHeader().getSyn();
            flags[2] = tcpPacket.getHeader().getRst();
            flags[3] = tcpPacket.getHeader().getPsh();
            flags[4] = tcpPacket.getHeader().getAck();
            flags[5] = tcpPacket.getHeader().getUrg();
            tcpPoint.setFlags(flags);
            point = tcpPoint;

        } else if(packet.contains(UdpPacket.class)) {
            // get payload length from UDPPacket
            int length = packet.get(UdpPacket.class).getPayload().length();
            point = new UDPPoint(length, time, isSent, ip);
        } else {
            point = new AnyPoint(packet.length(), time, isSent, ip);
        }
        return point;

    }
}