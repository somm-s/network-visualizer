package com.capture;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.hickup.points.IPPoint;

public class DataBaseIngestionThread extends Thread {

    SynchronizedRingBuffer buffer;
    int batchSize = 200; // Ingest 200 points at a time
    long timeout = 2; // 1 millisecond. If exceeded, ingest whatever is in the buffer
    String insertDataSQL = "INSERT INTO " + IPPoint.tableName + " (protocol, size, timestamp, src_ip, dst_ip, src_port, dst_port, FIN, SYN, RST, PSH, ACK, URG)"
        + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    long lastCommitTime = System.currentTimeMillis();
    private volatile boolean isRunning = true;


    public DataBaseIngestionThread(SynchronizedRingBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public void run() {
        System.out.println("DataBaseIngestionThread started");
        IPPoint.connect();
        
        // run sql command to create index:
        String srcIndex = "CREATE INDEX capture_src_ip_timestamp_idx ON live_packets (src_ip, timestamp)";
        String dstIndex = "CREATE INDEX capture_dst_ip_timestamp_idx ON live_packets (dst_ip, timestamp)";

        // send commands
        try {
            IPPoint.connection.createStatement().execute(srcIndex);
            IPPoint.connection.createStatement().execute(dstIndex);
        } catch (SQLException e) {}

        PreparedStatement preparedStatement;
        try {
            preparedStatement = IPPoint.connection.prepareStatement(insertDataSQL);
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        int currentBatch = 0;

        try {
            while (isRunning) {
                // add point to prepared statement
                IPPoint ipPoint = (IPPoint) buffer.consume();
                ipPoint.insertPointToSqlBatch(preparedStatement);
                currentBatch++;

                // if batch is full, execute batch
                if (currentBatch >= batchSize || System.currentTimeMillis() - lastCommitTime > timeout) {
                    preparedStatement.executeBatch();
                    preparedStatement.clearBatch();
                    currentBatch = 0;
                    lastCommitTime = System.currentTimeMillis();
                }
            }
        } catch (InterruptedException | SQLException e) {
        }
    }

    public void stopThread() {
        isRunning = false;
    }

}
