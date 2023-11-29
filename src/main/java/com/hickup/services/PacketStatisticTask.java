package com.hickup.services;

import org.pcap4j.core.PcapHandle;
import org.pcap4j.packet.Packet;

import com.hickup.Model;
import com.hickup.points.IPPoint;
import javafx.application.Platform;
import java.io.FileWriter;
import java.io.IOException;


// unused imports
// import java.text.SimpleDateFormat;
// import java.util.Date;
// import org.pcap4j.core.PcapNativeException;
// import org.pcap4j.core.PcapNetworkInterface;
// import org.pcap4j.core.Pcaps;

public class PacketStatisticTask extends PacketTask {


    FileWriter fileWriter;
    Model model = Model.getInstance();

    public PacketStatisticTask(final String fileName, final String filter, final String networkInterfaceName, final String receiverIP) {
        super(filter, networkInterfaceName, receiverIP);
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(fileName);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        this.fileWriter = fileWriter;
    }

    public void onCancel(PcapHandle handle) {
        
        
        try {
            model.writeStats(fileWriter);
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            System.out.println("Flushing Failed");
        }
        handle.close();
    }

    @Override
    public void processPacket(Packet packet, PcapHandle handle, boolean isSent, String ip) {

        // use IPPoint to parse packet and write to the file
        IPPoint p = IPPoint.parsePacket(packet, handle.getTimestamp());
        model.addPoint(p);
    }
}