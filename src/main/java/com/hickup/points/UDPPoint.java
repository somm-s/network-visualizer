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

    @Override
    public String toString() {
        // serialize to string
        String s = "";
        s += "1,"; // type
        s += packetSize + ",";
        s += IPPoint.timeToString(time) + ",";
        s += srcIp + ",";
        s += dstIp + ",";
        s += srcPort + ",";
        s += dstPort;
        return s;
    }
    
    public static UDPPoint fromString(String s) {
        // deserialize from string
        String[] parts = s.split(",");
        int packetSize = Integer.parseInt(parts[1]);
        Timestamp time = IPPoint.timeFromString(parts[2]);
        String srcIp = parts[3];
        String dstIp = parts[4];
        int srcPort = Integer.parseInt(parts[5]);
        int dstPort = Integer.parseInt(parts[6]);
        return new UDPPoint(packetSize, time, srcIp, dstIp, srcPort, dstPort);
    }
}
