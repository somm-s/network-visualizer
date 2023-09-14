package com.hickup;

import java.util.ArrayList;

import org.pcap4j.core.BpfProgram;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.Pcaps;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.Packet;
import java.util.Date;
import java.util.concurrent.TimeoutException;
import java.io.EOFException;
import java.text.SimpleDateFormat;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.Pcaps;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

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

    private ReadOnlyObjectWrapper<ObservableList<Data>> partialResults =
            new ReadOnlyObjectWrapper<>(this, "partialResults",
                    FXCollections.observableArrayList(new ArrayList<Data>()));

    public final ObservableList<Data> getPartialResults() { 
        return partialResults.get(); 
    }


    public final ReadOnlyObjectProperty<ObservableList<Data>> partialResultsProperty() {
        return partialResults.getReadOnlyProperty();
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
                    boolean isSent = false;
                    if(this.isSentPacket(packet)){
                        isSent = true;
                    }
                    int packetSize = packet.length();
                    final Data r = new Data(packetSize, handle.getTimestamp(), isSent);
                    Platform.runLater(new Runnable() { 
                        @Override public void run() {
                        // partialResults.get().add(r);
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