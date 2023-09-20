package com.hickup.services;

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
import javafx.concurrent.Task;

public abstract class PacketTask extends Task<Void> {

    final String filter;
    final String networkInterfaceName;
    final String receiverIP;

    public PacketTask(final String filter, final String networkInterfaceName, final String receiverIP) {
        this.filter = filter;
        this.networkInterfaceName = networkInterfaceName;
        this.receiverIP = receiverIP;
    }

    // override to get task for each packet
    public abstract void processPacket(Packet packet, PcapHandle handle, boolean isSent, String ip);

    // override to have other functionality. Default is to close handle
    public void onCancel(PcapHandle handle) {
        handle.close();
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

                    processPacket(packet, handle, isSent, ip);
                }
            } catch (PcapNativeException | NotOpenException e) {
                e.printStackTrace();
            }
        }
        onCancel(handle);
        return null;
    }
}