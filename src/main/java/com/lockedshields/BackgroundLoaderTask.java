package com.lockedshields;
import com.hickup.points.IPPoint;

import javafx.concurrent.Task;

public class BackgroundLoaderTask extends Task<Void> {

    private DataBuffer dataBuffer;

    public BackgroundLoaderTask(DataBuffer dataBuffer) {
        this.dataBuffer = dataBuffer;
    }

    @Override
    protected Void call() {
        // Background loading logic
        // ...

        // Once loading is complete, update the data buffer
        IPPoint[] loadedData = null;
        dataBuffer.setData(loadedData);

        // Optionally trigger a refresh or update of the custom canvas
        // customCanvas.render();

        return null;
    }
}
