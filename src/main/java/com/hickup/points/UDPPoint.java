package com.hickup.points;

import java.sql.Timestamp;

import javafx.scene.canvas.GraphicsContext;

public class UDPPoint extends IPPoint {

    int srcPort;
    int dstPort;

    public UDPPoint(int packetSize, Timestamp time, String srcIp, String dstIp) {
        super(packetSize, time, srcIp, dstIp);
    }

    public UDPPoint(int packetSize, Timestamp time, String srcIp, String dstIp, int srcPort, int dstPort) {
        super(packetSize, time, srcIp, dstIp);
        this.srcPort = srcPort;
        this.dstPort = dstPort;
    }

    @Override
    public void draw(GraphicsContext gc, double x, double y, double width, double height) {
        gc.fillRect(x - (width - 1) / 2, y - (height - 1) / 2, width - 1, height - 1);
    }
    
}
