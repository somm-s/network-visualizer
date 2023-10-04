package com.lockedshields;

import org.pcap4j.core.BpfProgram;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PacketListener;
// import pcap4j dependencies
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.Pcaps;
import org.pcap4j.packet.Packet;

import com.hickup.points.IPPoint;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.zip.GZIPInputStream;

public class PcapLoader {

    String pcapFilePath = "/home/lab/Documents/networking/hickup-net/kolka-220418-00000101.pcap.gz";
    String tempFileName = "temp.pcap";

    public PcapLoader(String pcapFilePath) {
        this.pcapFilePath = pcapFilePath;
    }

    public PcapLoader(String pcapFilePath, String tempFileName) {
        this.pcapFilePath = pcapFilePath;
        this.tempFileName = tempFileName;
    }

    public void loadDataFromPcap(LinkedList<IPPoint> data) {
        
        // Read the PCAP file
        try {

            // decompress pcap.gz file at PCAP_FILE_PATH and store it in a temporary file
            PcapDecompressor.decompress(pcapFilePath, tempFileName);

            PcapHandle handle = Pcaps.openOffline(tempFileName, PcapHandle.TimestampPrecision.NANO);
            
            handle.setFilter("ip", BpfProgram.BpfCompileMode.OPTIMIZE);
            PacketListener pl = new PacketListener() {
                @Override
                public void gotPacket(Packet packet) {
                    
                    // parse packet to IPPoint
                    IPPoint point = IPPoint.parsePacket(packet, handle.getTimestamp());
                    if (point != null) {
                        data.add(point);
                    }
                }
            };
            handle.loop(-1, pl);

            // delete the temporary file
            File file = new File(tempFileName);
            file.delete();

        } catch (PcapNativeException | NotOpenException | IOException | InterruptedException e) {
            e.printStackTrace();
            System.out.println("Fail!");
        }

        System.out.println("Completed!");
    }
}

class PcapDecompressor {

    // method to decompress pcap.gz file and store it in a temporary file. Returns the temporary file name.
    public static void decompress(String filename, String tempname) throws IOException {
        // Open the compressed file
        FileInputStream fis = new FileInputStream(filename);
        GZIPInputStream gzis = new GZIPInputStream(fis);

        // Create a temporary file to store the decompressed data
        FileOutputStream fos = new FileOutputStream(tempname);
        byte[] buffer = new byte[1024];
        int len;
        while ((len = gzis.read(buffer)) > 0) {
            fos.write(buffer, 0, len);
        }

        // Close the streams
        gzis.close();
        fis.close();
        fos.close();
    }
}