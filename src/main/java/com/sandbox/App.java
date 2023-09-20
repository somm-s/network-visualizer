package com.sandbox;

import javax.swing.*;
import java.io.IOException;

import org.pcap4j.core.*;
import org.pcap4j.packet.IpPacket;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.namednumber.IpVersion;
import org.pcap4j.*;
import org.pcap4j.util.NifSelector;
import org.pcap4j.util.Packets;

public class App 
{
    
    public static void main( String[] args ) throws IOException, PcapNativeException, NotOpenException
    {
        
        // JFrame frame = new JFrame("Hickup");
        // frame.setSize(300, 300);
        // frame.setVisible(true);
        // frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        // Add a panel to the JFrame that has a selector to select the network interface where to listen


        NifSelector nifSelector = new NifSelector();
        PcapNetworkInterface nif = nifSelector.selectNetworkInterface();
        if (nif == null) {
            return;
        }
        System.out.println(nif.getName() + "(" + nif.getDescription() + ")");        

        PcapNetworkInterface.PromiscuousMode mode = PcapNetworkInterface.PromiscuousMode.PROMISCUOUS;

        int timeout = 10;
        PcapHandle handle = nif.openLive(65536, mode, timeout);
        PacketListener listener = new PacketListener() {

            @Override
            public void gotPacket(Packet packet) {
                // System.out.println(packet);

                if (Packets.containsTcpPacket(packet)) {
                    System.out.println("TCP packet");
                }
            }

        };
        try {
            int maxPackets = 50;
            handle.loop(maxPackets, listener);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Packet packet = handle.getNextPacket();
        if (packet != null) {
            System.out.println(packet);
        }
        handle.close();
    }
}
