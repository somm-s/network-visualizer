package com.lockedshields;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.pcap4j.core.BpfProgram;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PacketListener;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapHandle.TimestampPrecision;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.Pcaps;
import org.pcap4j.packet.Packet;
import com.hickup.points.IPPoint;

public class IPWriter {
    
    String PCAP_FOLDER_PATH;
    String OUTPUT_PATH;
    String FILTER;

    public IPWriter(String PCAP_FOLDER_PATH, String OUTPUT_PATH, String FILTER) {
        this.PCAP_FOLDER_PATH = PCAP_FOLDER_PATH;
        this.OUTPUT_PATH = OUTPUT_PATH;
        this.FILTER = FILTER;
    }

    public static void main(String[] args) {
        String PCAP_FOLDER_PATH = "/home/lab/Documents/networking/hickup-net/pcaps";
        String OUTPUT_PATH = "/home/lab/Documents/networking/hickup-net/pcap.ip.csv";
        String FILTER = "host 10.3.8.34";

        IPWriter writer = new IPWriter(PCAP_FOLDER_PATH, OUTPUT_PATH, FILTER);
        try {
            writer.writeFromPcap();
        } catch (IOException e) {
            System.out.println("Couldn't write to csv file: " + OUTPUT_PATH);
            e.printStackTrace();
        }
    }

    public void writeFromPcap() throws IOException {

        // get a list with all files in folder:
        File folder = new File(PCAP_FOLDER_PATH);
        System.out.println(folder.listFiles());
        File[] listOfFiles = folder.listFiles();
        // System.out.println(listOfFiles.length);
        // System.out.println(listOfFiles.length + " " + listOfFiles[0].getAbsolutePath());

        // sort list to have the files in the correct order
        java.util.Arrays.sort(listOfFiles);

        // open the new csv file to write the statistics to. use opencsv library
        File csvFile = new File(OUTPUT_PATH);
        FileWriter writer = new FileWriter(csvFile, true);


        // iterate over all pcap files, decompress them and write the extracted statistics to the csv file
        for (int i = 0; i < listOfFiles.length; i++) {
            String pcapFilePath = listOfFiles[i].getAbsolutePath();
            System.out.println(pcapFilePath);

            // decompress pcap.gz file
            String tempFileName = "temp.pcap";
            
            try {
                PcapDecompressor.decompress(pcapFilePath, tempFileName);
            } catch(Exception e) {
                System.out.println("Couldn't decompress pcap file: " + listOfFiles[i].getName());
                continue;
            }


            // Open the pcap file
            PcapHandle handle;

            try {
                handle = Pcaps.openOffline(tempFileName, TimestampPrecision.NANO);
                // add filter
                handle.setFilter(FILTER, BpfProgram.BpfCompileMode.OPTIMIZE);
            } catch (PcapNativeException | NotOpenException e) {
                System.out.println("Couldn't open pcap file: " + tempFileName);
                e.printStackTrace();
                return;
            }

            // create a packet listener
            PacketListener pl = new PacketListener() {
                @Override
                public void gotPacket(Packet packet) {
                    // parse packet to IPPoint
                    IPPoint point = IPPoint.parsePacket(packet, handle.getTimestamp());
                    if (point != null) {
                        // write to file
                        try {
                            writer.write(point.toString() + "\n");
                        } catch (IOException e) {
                            System.out.println("Couldn't write to csv file: " + OUTPUT_PATH);
                            e.printStackTrace();
                        }
                    }
                }
            };

            // loop over all packets in the pcap file
            try {
                handle.loop(-1, pl);
            } catch (PcapNativeException | InterruptedException | NotOpenException e) {
                System.out.println("Couldn't loop over pcap file: " + tempFileName);
                e.printStackTrace();
            }

            // Close the handle
            handle.close();
            
            // delete the temporary file
            File file = new File(tempFileName);
            file.delete();

            // flush the writer
            writer.flush();
        }
    }

}
