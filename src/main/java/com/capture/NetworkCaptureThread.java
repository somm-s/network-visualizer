package com.capture;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.Pcaps;
import org.pcap4j.packet.Packet;

import com.hickup.points.IPPoint;

import java.io.EOFException;
import java.io.IOException;
import java.util.concurrent.TimeoutException;


public class NetworkCaptureThread extends Thread {

    
    private PcapHandle pcapHandle;
    private SynchronizedRingBuffer buffer;
    private volatile boolean isRunning = true;

    public NetworkCaptureThread(String networkInterface, SynchronizedRingBuffer buffer) throws PcapNativeException, NotOpenException {
        PcapNetworkInterface device = Pcaps.getDevByName(networkInterface);
        this.pcapHandle = device.openLive(65536, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, 10);
        this.buffer = buffer;
    }

    @Override
    public void run() {
        System.out.println("NetworkCaptureThread started");
        try {
            while (isRunning) {
                Packet packet = pcapHandle.getNextPacketEx();
                
                // extract IPPacket
                IPPoint ipPacket = IPPoint.parsePacket(packet, pcapHandle.getTimestamp());
                if (ipPacket != null) {
                    if(!buffer.produce(ipPacket)) {
                        System.out.println("Buffer is full!");
                    }
                }
            }
        } catch (PcapNativeException | NotOpenException | EOFException | TimeoutException e) {
            e.printStackTrace();
        } finally {
            pcapHandle.close();
        }
    }

    public void stopThread() {
        isRunning = false;
    }

    public static void main(String[] args) throws InterruptedException, PcapNativeException, NotOpenException, IOException {
        SynchronizedRingBuffer buffer = new SynchronizedRingBuffer(10000);

        // create DataBaseIngestionThread
        DataBaseIngestionThread ingestionThread = new DataBaseIngestionThread(buffer);
        ingestionThread.start();
        
        String networkInterface = "wlp0s20f3"; // Replace with your actual network interface
        NetworkCaptureThread captureThread = new NetworkCaptureThread(networkInterface, buffer);
        captureThread.start();

        // wait until enter is pressed
        System.out.println("Press enter to stop");
        System.in.read();

        // Stop the threads
        captureThread.stopThread();
        ingestionThread.stopThread();

        captureThread.join(); // Wait for the capture thread to finish
        ingestionThread.join(); // Wait for the ingestion thread to finish

        System.out.println("Done!");
    }
}
