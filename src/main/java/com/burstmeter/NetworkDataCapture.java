package com.burstmeter;

import org.pcap4j.core.BpfProgram;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PacketListener;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.Pcaps;
import org.pcap4j.packet.Packet;

import com.hickup.points.IPPoint;

import javafx.application.Platform;
import javafx.concurrent.Task;

public class NetworkDataCapture extends Task<Void> {
    private PcapHandle handle;

    @Override
    public Void call() throws Exception {
        PcapNetworkInterface networkInterface = null;

        // Set up the network interface for packet capture
        try {
            networkInterface = Pcaps.getDevByName("wlp0s20f3"); // wlp0s20f3 vboxnet0
        } catch (PcapNativeException e) {
            e.printStackTrace();
            System.exit(1);
        }
        BpfProgram.BpfCompileMode mode = BpfProgram.BpfCompileMode.OPTIMIZE;
        handle = networkInterface.openLive(65536, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, 10);
        handle.setFilter("ip", mode);

        PacketListener pl = new PacketListener() {
            @Override
            public void gotPacket(Packet packet) {
                // parse packet to IPPoint
                IPPoint point = IPPoint.parsePacket(packet, handle.getTimestamp());
                if (point != null) {
                    
                    // add point to model
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            Model.getInstance().addPoint(point);
                        }
                    });
                }
            }
        };
        handle.loop(-1, pl);

        System.out.println("Completed!");
        return null;
    }

    @Override
    protected void cancelled() {
        super.cancelled();
        if (handle != null) {
            try {
                handle.breakLoop();
            } catch (NotOpenException e) {
                e.printStackTrace();
            }
        }
    }

}
