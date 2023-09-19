package com.hickup;

import org.pcap4j.core.BpfProgram;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.Pcaps;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.Packet;
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


                    int packetSize = packet.length();
                    final Data r = new Data(packetSize, handle.getTimestamp(), isSent, ip);
                    Platform.runLater(new Runnable() { 
                        @Override public void run() {
                            service.setCapturedData(r);
                        }
                    });
                }
            } catch (PcapNativeException | NotOpenException e) {
                e.printStackTrace();
            }
        }

        for (int i=0; i<100; i++) {
            if (isCancelled()) break;





        }
        return null;
    }

    private boolean isSentPacket(Packet packet) {
        // Check if the packet contains an IPv4 packet (you can adapt this for IPv6 if needed)
        if (packet.contains(IpV4Packet.class)) {
            IpV4Packet ipV4Packet = packet.get(IpV4Packet.class);
            
            // Get the source IP address from the IPv4 packet
            String sourceIpAddress = ipV4Packet.getHeader().getSrcAddr().getHostAddress();

            // Compare the source IP address with the local IP address
            if (sourceIpAddress.equals(receiverIP)) {
                return true; // The packet was sent from the local system
            }
        } else {
            // TODO: implement for other packets as well.
        }

        return false; // The packet was not sent from the local system
    }
}