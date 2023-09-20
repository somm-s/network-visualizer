package com.hickup.points;

import javafx.scene.canvas.GraphicsContext;

public abstract class IPPoint {
    public int packetSize;
    public java.sql.Timestamp time;
    public boolean outgoing;
    public String ip;
    
    public IPPoint(int packetSize, java.sql.Timestamp time, boolean outgoing, String ip) {
        this.packetSize = packetSize;
        this.time = time;
        this.outgoing = outgoing;
        this.ip = ip;
    }

    public void draw(GraphicsContext gc, double x, double y, double width, double height) {
        gc.fillOval(x - width / 2, y - height / 2, width, height);
    }

    public String toString() {
        return "val: " + packetSize + " time: " + time;
    }
}


class Data {
    double val;
    java.sql.Timestamp time;
    boolean isSent;
    String ip;

    Data(double val, java.sql.Timestamp time, boolean isSent, String ip) {
        this.val = val;
        this.time = time;
        this.isSent = isSent;
        this.ip = ip;
    }

    Data(double val, java.sql.Timestamp time) {
        this.val = val;
        this.time = time;
        this.isSent = false;
    }


}