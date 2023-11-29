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

public class RecursivePcapConversionTask extends RecursiveTask<Void>{
    
    private final String outputPath;
    private final String filter;

    private File[] pcapFiles; 
    private BufferedWriter writer;

    private int start;
    private int end;

    /**
     * Recursive task to convert pcap files to csv files containing IPPoints.
     * 
     * @param outputPath absolute path to folder where output files will be written
     * @param filter filter to apply to pcap files
     * @param pcapFiles array of pcap files to process
     * @param start index of first pcap file to process
     * @param end index past last pcap file to process
     */
    public RecursivePcapConversionTask(String outputPath, String filter, File[] pcapFiles, int start, int end) {
        this.outputPath = outputPath;
        this.filter = filter;
        this.pcapFiles = pcapFiles;
        this.start = start;
        this.end = end;
    }

    public static void main(String[] args) {
        String pcapFolderPath = "/home/lab/Documents/networking/hickup-net/pcaps_diverse";
        String outputPath = "/home/lab/Documents/networking/hickup-net/output";
        // String PCAP_FOLDER_PATH = "/media/sosi/490d065d-ed08-4c6e-abd4-184715f06052/2022/BT03-CHE/pcaps";
        // String OUTPUT_PATH = "/media/sosi/490d065d-ed08-4c6e-abd4-184715f06052/2022/BT03-CHE/ippointsV6";

        String filter = "";

        convertPcaps(pcapFolderPath, outputPath, filter);
    }

    /**
     * Converts all pcap files in a folder to csv files containing IPPoints.
     * 
     * @param pcapFolderPath absolute path to folder containing pcap files
     * @param outputPath absolute path to folder where output files will be written
     * @param filter filter to apply to pcap files
     */
    public static void convertPcaps(String pcapFolderPath, String outputPath, String filter) {
        ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();
        File folder = new File(pcapFolderPath);
        System.out.println(folder.listFiles());
        File[] listOfFiles = folder.listFiles();

        java.util.Arrays.sort(listOfFiles);

        RecursivePcapConversionTask task = new RecursivePcapConversionTask(outputPath, filter, listOfFiles, 0, listOfFiles.length);

        forkJoinPool.invoke(task);
        forkJoinPool.shutdown();
    }

    @Override
    protected Void compute() {
        if(end - start > 1) {
            reduce(); // split into two tasks and combine results
        } else {
            map(); // process pcap file
        }
        return null;
    }

    private boolean hasFileEnding(File file, String ending) {
        String fileName = file.getName();

        if(fileName.length() < ending.length()) {
            return false;
        }

        String lastPart = fileName.substring(fileName.length() - ending.length() - 1, fileName.length());
        return lastPart.equals("." + ending);
    }

    private boolean hasAllowedEnding(File file, String[] allowedEndings) {
        for(String ending : allowedEndings) {
            if(hasFileEnding(file, ending)) {
                return true;
            }
        }
        return false;
    }

    private String combinePaths(String directory, String file) {
        return Paths.get(directory).resolve(file).toString();
    }

    private String combinePaths(String[] paths, String file) {
        String path = paths[0];
        for(int i = 1; i < paths.length; i++) {
            path = Paths.get(path).resolve(paths[i]).toString();
        }
        return Paths.get(path).resolve(file).toString();
    }

    private void map() {

        int pcapIndex = this.start;
        System.out.println("Worker " + Thread.currentThread().getId() + " processing " + pcapFiles[pcapIndex].getName());

        String pcapFilePath = pcapFiles[pcapIndex].getAbsolutePath();

        if(!hasAllowedEnding(pcapFiles[pcapIndex], new String[] {"pcap", "pcap.gz"})) {
            System.out.println("File " + pcapFiles[pcapIndex].getName() + " has wrong file ending, skipping...");
            return;
        }

        // use "start" as unique identifier for processed pcap file
        File pcapOutputDirectory = new File(combinePaths(outputPath, Integer.toString(pcapIndex)));
        pcapOutputDirectory.mkdirs();

        if(pcapFiles[pcapIndex].getName().endsWith(".pcap.gz")) {
            // decompress pcap file
            String tempFilePath = combinePaths(pcapOutputDirectory.getAbsolutePath(), "temp.pcap");
            try {
                PcapDecompressor.decompress(pcapFilePath, tempFilePath);
            } catch(Exception e) {
                System.out.println("Couldn't decompress pcap file: " + pcapFiles[pcapIndex].getName() + ", skipping...");
                return;
            }
            pcapFilePath = tempFilePath;
        }

        // Open the pcap file
        PcapHandle handle;

        try {
            handle = Pcaps.openOffline(pcapFilePath, TimestampPrecision.MICRO);
            // add filter
            handle.setFilter(filter, BpfProgram.BpfCompileMode.OPTIMIZE);
        } catch (PcapNativeException | NotOpenException e) {
            System.out.println("Couldn't open pcap file: " + pcapFilePath + ", skipping...");
            return;
        }

        // TODO: change, currently we assume that the pcap file is sorted by time
        // TODO: implement bucket sort to improve performance
        
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
                File outFile = new File(Paths.get(pcapOutputDirectory.getAbsolutePath()).resolve(outFileName).toString());

                // check if file exists, create new filewriter and close previous one if not null
                try {
                    if(!outFile.exists()) {
                        if(writer != null) {
                            writer.flush();
                            writer.close();
                        }
                        writer = new BufferedWriter(new FileWriter(outFile, true));
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
            System.out.println("Couldn't loop over pcap file: " + pcapFilePath);
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
        File file = new File(pcapFilePath);
        file.delete();

        // System.out.println("Worker " + threadId + " Finished " + pcapFiles[start].getName());

    }

    private void reduce() {

        int mid = (start + end) / 2;
        RecursivePcapConversionTask leftTask = new RecursivePcapConversionTask(outputPath, filter, pcapFiles, start, mid);
        RecursivePcapConversionTask rightTask = new RecursivePcapConversionTask(outputPath, filter, pcapFiles, mid, end);
        
        leftTask.fork();
        rightTask.fork();
        leftTask.join();
        rightTask.join();

        // System.out.println("Worker " + threadId + " Combining files " + start + " and " + mid);

        // the results will be in start and mid, respectively. 
        File firstDirectory = new File(Paths.get(outputPath).resolve(Integer.toString(start)).toString());
        File secondDirectory = new File(Paths.get(outputPath).resolve(Integer.toString(mid)).toString());

        // Start by identifying conflicting files.
        File[] firstFiles = firstDirectory.listFiles();
        File[] secondFiles = secondDirectory.listFiles();

        List<File> conflictingFiles = new ArrayList<>();
        if(firstFiles != null && secondFiles != null) {
            java.util.Arrays.sort(firstFiles);
            java.util.Arrays.sort(secondFiles);
    
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
                return;
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

        // System.out.println("Worker " + threadId + " Finished Combining " + start + " and " + mid);

        return;
    }



}
