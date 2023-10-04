package com.lockedshields;

import org.pcap4j.core.BpfProgram;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PacketListener;
// import pcap4j dependencies
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.Pcaps;
import org.pcap4j.packet.Packet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

public class PcapLoader {

    private static final String PCAP_FILE_PATH = "/home/lab/Documents/networking/hickup-net/kolka-220418-00000101.pcap.gz";

    public static void main(String[] args) {
        System.out.println("Hello, World!");

        // Read the PCAP file
        try {

            // decompress pcap.gz file at PCAP_FILE_PATH and store it in a temporary file
            String tempFile = PcapDecompressor.decompress(PCAP_FILE_PATH);

            PcapHandle handle = Pcaps.openOffline(tempFile);

            handle.setFilter("ip", BpfProgram.BpfCompileMode.OPTIMIZE);
            PacketListener pl = new PacketListener() {
                @Override
                public void gotPacket(Packet packet) {
                    double packetSize = Math.log(packet.length()) - 4;
                    System.out.println(packetSize);
                }
            };
            handle.loop(-1, pl);

            // delete the temporary file
            File file = new File("temp.pcap");
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
    public static String decompress(String filename) throws IOException {
        // Open the compressed file
        FileInputStream fis = new FileInputStream(filename);
        GZIPInputStream gzis = new GZIPInputStream(fis);

        // Create a temporary file to store the decompressed data
        FileOutputStream fos = new FileOutputStream("temp.pcap");
        byte[] buffer = new byte[1024];
        int len;
        while ((len = gzis.read(buffer)) > 0) {
            fos.write(buffer, 0, len);
        }

        // Close the streams
        gzis.close();
        fis.close();
        fos.close();
        return "temp.pcap";
    }
}