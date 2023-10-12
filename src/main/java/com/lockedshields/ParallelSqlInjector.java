package com.lockedshields;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.PreparedStatement;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

import com.hickup.points.IPPoint;

public class ParallelSqlInjector extends RecursiveTask<Void>{
    
    File[] listOfFiles;
    int start;
    int end;
    String PCAP_FOLDER_PATH;
    String insertDataSQL = "INSERT INTO packets (timestamp, protocol, size, src_ip, dst_ip, src_port, dst_port, FIN, SYN, RST, PSH, ACK, URG)"
        + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public ParallelSqlInjector(String pcapFolderPath, File[] listOfFiles, int start, int end) {
        this.listOfFiles = listOfFiles;
        this.start = start;
        this.end = end;
        this.PCAP_FOLDER_PATH = pcapFolderPath;
    }

    public static void main(String[] args) {

        IPPoint.connect();
        String PCAP_FOLDER_PATH = "/home/lab/Documents/networking/ls22/0";

        // Create a ForkJoinPool
        ForkJoinPool forkJoinPool = new ForkJoinPool(16);

        // get a list with all files in folder:
        File folder = new File(PCAP_FOLDER_PATH);
        System.out.println(folder.listFiles());
        File[] listOfFiles = folder.listFiles();
        // sort list to have the files in the correct order
        java.util.Arrays.sort(listOfFiles);

        // Create a ForkJoinTask for the main computation
        ParallelSqlInjector task = new ParallelSqlInjector(PCAP_FOLDER_PATH, listOfFiles, 0, listOfFiles.length);

        // Execute the task and get the result
        forkJoinPool.invoke(task);

        // Shutdown the ForkJoinPool
        forkJoinPool.shutdown();

    }

    @Override
    protected Void compute() {

        long threadId = Thread.currentThread().getId();
        // System.out.println("Start: " + start + " end: " + end + " worker " + threadId);

        if(end - start > 1) { // split and combine
            
            int mid = (start + end) / 2;
            ParallelSqlInjector leftTask = new ParallelSqlInjector(PCAP_FOLDER_PATH, listOfFiles, start, mid);
            ParallelSqlInjector rightTask = new ParallelSqlInjector(PCAP_FOLDER_PATH, listOfFiles, mid, end);            
            leftTask.fork();
            rightTask.fork();
            leftTask.join();
            rightTask.join();

        } else {

            try {
                System.out.println("Worker " + threadId + " reading " + listOfFiles[start].getName());
                BufferedReader reader = new BufferedReader(new FileReader(listOfFiles[start]));
                String line = reader.readLine();
                PreparedStatement preparedStatement = IPPoint.connection.prepareStatement(insertDataSQL);

                // iterator
                IPPoint res = null;
                int numPackets = 0;

                while(line != null && !line.equals("")) {
                    // apply filter:
                    res = IPPoint.fromString(line);
                    res.insertPointToSqlBatch(preparedStatement);
                    line = reader.readLine();
                    numPackets++;

                    if(numPackets % 3000 == 0) {
                        preparedStatement.executeBatch();
                        preparedStatement.clearBatch();
                    }
                }    
                preparedStatement.executeBatch();
                preparedStatement.clearBatch(); 
                System.out.println("Worker " + threadId + " ingested " + numPackets + " Packets");
                reader.close();
            } catch(Exception e) {
                System.out.println("Worker " + threadId + " Exception while reading " + listOfFiles[start].getName());
            }
        }

        return null;

    }
}
