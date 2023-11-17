package com.lockedshields;
import com.hickup.points.IPPoint;

import javafx.concurrent.Task;

public class BackgroundLoaderTask extends Task<Void> {

    private DataBuffer dataBuffer;
    private String startTime;
    private String endTime;
    private String observer;
    private String dstHost;

    public BackgroundLoaderTask(DataBuffer dataBuffer, String startTime, String endTime, String observer, String dstHost) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.observer = observer;
        this.dstHost = dstHost;
        this.dataBuffer = dataBuffer;
    }

    @Override
    protected Void call() {

        // connecting to database
        IPPoint.connect();

        System.out.println("Loading data from database");
        // sql query retreiving all packets from table 'packets'

        IPPoint[] data = IPPoint.getPointsFromSQL(startTime, endTime, observer, dstHost, 0);
        System.out.println("Data loaded from database");
        dataBuffer.setData(data, observer);

        return null;
    }
}
