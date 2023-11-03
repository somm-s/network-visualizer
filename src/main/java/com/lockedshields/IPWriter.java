package com.lockedshields;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

import org.pcap4j.core.BpfProgram;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PacketListener;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapHandle.TimestampPrecision;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.Pcaps;
import org.pcap4j.packet.Packet;
import com.hickup.points.IPPoint;

public class IPWriter extends RecursiveTask<Void>{
    
    String PCAP_FOLDER_PATH;
    String OUTPUT_PATH;
    String FILTER;
    File[] pcapFiles; 
    private int start;
    private int end;
    FileWriter writer = null;

    public IPWriter(String PCAP_FOLDER_PATH, String OUTPUT_PATH, String FILTER, File[] pcapFiles, int start, int end) {
        this.PCAP_FOLDER_PATH = PCAP_FOLDER_PATH;
        this.OUTPUT_PATH = OUTPUT_PATH;
        this.FILTER = FILTER;
        this.pcapFiles = pcapFiles;
        this.start = start;
        this.end = end;
    }

    public static void main(String[] args) {
        // String PCAP_FOLDER_PATH = "/home/lab/Documents/networking/hickup-net/pcaps_diverse";
        // String OUTPUT_PATH = "/home/lab/Documents/networking/hickup-net/output";
        // String PCAP_FOLDER_PATH = "/media/sosi/490d065d-ed08-4c6e-abd4-184715f06052/2022/BT03-CHE/pcaps";
        // String OUTPUT_PATH = "/media/sosi/490d065d-ed08-4c6e-abd4-184715f06052/2022/BT03-CHE/ippoints";

        String PCAP_FOLDER_PATH = "pcaps";
        String OUTPUT_PATH = "output";

        String FILTER = "";

        // Create a ForkJoinPool
        ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();

        // get a list with all files in folder:
        File folder = new File(PCAP_FOLDER_PATH);
        System.out.println(folder.listFiles());
        File[] listOfFiles = folder.listFiles();
        // sort list to have the files in the correct order
        java.util.Arrays.sort(listOfFiles);

        // Create a ForkJoinTask for the main computation
        IPWriter task = new IPWriter(PCAP_FOLDER_PATH, OUTPUT_PATH, FILTER, listOfFiles, 0, listOfFiles.length);

        // Execute the task and get the result
        forkJoinPool.invoke(task);

        // Shutdown the ForkJoinPool
        forkJoinPool.shutdown();
    }

    @Override
    protected Void compute() {
        long threadId = Thread.currentThread().getId();

        if(end - start > 1) { // split and combine
            
            int mid = (start + end) / 2;
            IPWriter leftTask = new IPWriter(PCAP_FOLDER_PATH, OUTPUT_PATH, FILTER, pcapFiles, start, mid); // mid exclusive
            IPWriter rightTask = new IPWriter(PCAP_FOLDER_PATH, OUTPUT_PATH, FILTER, pcapFiles, mid, end); // mid exclusive
            
            leftTask.fork();
            rightTask.fork();
            leftTask.join();
            rightTask.join();

            System.out.println("Worker " + threadId + " Combining files " + start + " and " + mid);

            // the results will be in start and mid, respectively. 
            File firstDirectory = new File(Paths.get(OUTPUT_PATH).resolve(Integer.toString(start)).toString());
            File secondDirectory = new File(Paths.get(OUTPUT_PATH).resolve(Integer.toString(mid)).toString());

            // Start by identifying conflicting files.
            File[] firstFiles = firstDirectory.listFiles();
            File[] secondFiles = secondDirectory.listFiles();
            java.util.Arrays.sort(firstFiles);
            java.util.Arrays.sort(secondFiles);

            List<File> conflictingFiles = new ArrayList<>();
            int i = 0; int j = 0;
            while(i < firstFiles.length && j < secondFiles.length) {
                String a = firstFiles[i].getName();
                String b = secondFiles[j].getName();
                if(a.equals(b)) {
                    conflictingFiles.add(firstFiles[i]);
                    i++; j++;
                } else if(a.compareTo(b) < 0) {
                    i++;
                } else  {
                    j++;
                }
            }

            // conflicting files are opened simultaneously and iterated over at the same time (merge)
            for(File conflictFile : conflictingFiles) {
                // open writer to write into start folder:
                File newFilePath = new File(Paths.get(firstDirectory.getAbsolutePath()).resolve("resolved " + conflictFile.getName()).toString());
                try {
                    BufferedWriter combineWriter = new BufferedWriter(new FileWriter(newFilePath));

                    // file 1
                    File file1 = new File(Paths.get(firstDirectory.getAbsolutePath()).resolve(conflictFile.getName()).toString());
                    BufferedReader reader1 = new BufferedReader(new FileReader(file1));
                    
                    // file 2
                    File file2 = new File(Paths.get(secondDirectory.getAbsolutePath()).resolve(conflictFile.getName()).toString());
                    BufferedReader reader2 = new BufferedReader(new FileReader(file2));

                    String line1 = reader1.readLine();
                    String line2 = reader2.readLine();
                    IPPoint p1 = null;
                    IPPoint p2 = null;

                    while(line1 != null && !line1.equals("") && line2 != null && !line2.equals("")) {
                        p1 = IPPoint.fromString(line1);
                        p2 = IPPoint.fromString(line2);

                        if(p1.time.getTime() < p2.time.getTime()) {
                            // write p1
                            combineWriter.write(p1.toString() + "\n");
                            line1 = reader1.readLine();
                        } else {
                            // write p2
                            combineWriter.write(p2.toString() + "\n");
                            line2 = reader2.readLine();
                        }
                    }

                    while(line1 != null && !line1.equals("")) {
                        p1 = IPPoint.fromString(line1);
                        combineWriter.write(p1.toString() + "\n");
                        line1 = reader1.readLine();
                    }

                    while(line2 != null && !line2.equals("")) {
                        p2 = IPPoint.fromString(line2);
                        combineWriter.write(p2.toString() + "\n");
                        line2 = reader2.readLine();
                    }

                    combineWriter.flush();
                    combineWriter.close();
                    reader1.close();
                    reader2.close();
                } catch (IOException e) {
                    System.out.println("Merging failed. Skipping...");
                    e.printStackTrace();
                    return null;
                }
            }
            
            // non-conflicting files are simply moved to the start directory
            for(File secondFile : secondFiles) {
                try {
                    Files.move(Paths.get(secondFile.getAbsolutePath()), 
                    Paths.get(firstDirectory.getAbsolutePath()).resolve(secondFile.getName()), 
                    StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    System.out.println("coudn't replace " + secondFile.getAbsolutePath());
                }
            }

            // conflict files are moved to the directory, also replacing:
            for(File conflictFile : conflictingFiles) {
                try {
                    Files.move(Paths.get(firstDirectory.getAbsolutePath()).resolve("resolved " + conflictFile.getName()), 
                    Paths.get(conflictFile.getAbsolutePath()), 
                    StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    System.out.println("coudn't replace resolved to " + conflictFile.getAbsolutePath());
                }
            }

            System.out.println("Worker " + threadId + " Finished Combining " + start + " and " + mid);

            return null;
        }

        // create folder for pcap file in output path:
        File pcapDirectory = new File(Paths.get(OUTPUT_PATH).resolve(Integer.toString(start)).toString());
        pcapDirectory.mkdirs();

        String pcapFilePath = pcapFiles[start].getAbsolutePath();
        System.out.println("Worker " + threadId + " processing " + pcapFiles[start].getName());

        // decompress pcap.gz file
        String tempFileName = Paths.get(pcapDirectory.getAbsolutePath()).resolve("temp.pcap").toString();
        
        try {
            PcapDecompressor.decompress(pcapFilePath, tempFileName);
        } catch(Exception e) {
            System.out.println("Couldn't decompress pcap file: " + pcapFiles[start].getName());
            return null;
        }


        // Open the pcap file
        PcapHandle handle;

        try {
            handle = Pcaps.openOffline(tempFileName, TimestampPrecision.MICRO);
            // add filter
            handle.setFilter(FILTER, BpfProgram.BpfCompileMode.OPTIMIZE);
        } catch (PcapNativeException | NotOpenException e) {
            System.out.println("Couldn't open pcap file: " + tempFileName);
            e.printStackTrace();
            return null;
        }
        
        // create a packet listener
        PacketListener pl = new PacketListener() {
            @Override
            public void gotPacket(Packet packet) {

                // parse packet to IPPoint
                IPPoint point = IPPoint.parsePacket(packet, handle.getTimestamp());
                if(point == null) {
                    return;
                }
                
                // get filename corresponding to packet:
                String timeString = IPPoint.timeToString(point.time);
                int colonIndex = timeString.indexOf(':');
                String outFileName = timeString.substring(0, colonIndex + 3) + ".csv";
                File outFile = new File(Paths.get(pcapDirectory.getAbsolutePath()).resolve(outFileName).toString());

                // check if file exists, create new filewriter and close previous one if not null
                try {
                    if(!outFile.exists()) {
                        if(writer != null) {
                            writer.flush();
                            writer.close();
                        }
                        writer = new FileWriter(outFile, true);
                    }
                    writer.write(point.toString() + "\n");
                } catch (IOException e) {
                    System.out.println("Couldn't write point to: " + outFile.getAbsolutePath());
                    e.printStackTrace();
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

        // close writer if not null
        if(writer != null) {
            try {
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        // delete the temporary file
        File file = new File(tempFileName);
        file.delete();

        System.out.println("Worker " + threadId + " Finished " + pcapFiles[start].getName());


        return null;
    }

}
