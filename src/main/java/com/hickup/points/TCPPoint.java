package com.hickup.points;

import java.sql.Timestamp;

import javafx.scene.canvas.GraphicsContext;

public class TCPPoint extends IPPoint {

    boolean[] flags = new boolean[6]; // FIN, SYN, RST, PSH, ACK, URG
    int srcPort;
    int dstPort;

    public TCPPoint(int packetSize, Timestamp time, String srcIp, String dstIp) {
        super(packetSize, time, srcIp, dstIp);
    }

    public TCPPoint(int packetSize, Timestamp time, String srcIp, String dstIp, int srcPort, int dstPort) {
        super(packetSize, time, srcIp, dstIp);
        this.srcPort = srcPort;
        this.dstPort = dstPort;
    }


    @Override
    public void draw(GraphicsContext gc, double x, double y, double width, double height) {

        super.draw(gc, x, y, width, height);
        width = width + 1;
        height = height + 1;
        if(flags[1]) {
            // draw cross with (x, y) in the center if SYN flag is set
            gc.setStroke(javafx.scene.paint.Color.BLACK);
            gc.strokeLine(x - width / 2, y - height / 2, x + width / 2, y + height / 2);
            gc.strokeLine(x - width / 2, y + height / 2, x + width / 2, y - height / 2);
        } else if(flags[2]) {
            // draw cross with (x, y) in the center if RST flag is set
            gc.setStroke(javafx.scene.paint.Color.WHITE);
            gc.strokeLine(x - width / 2, y - height / 2, x + width / 2, y + height / 2);
            gc.strokeLine(x - width / 2, y + height / 2, x + width / 2, y - height / 2);
        } else if(flags[0]) {
            // draw horizontal cross with (x, y) in the center if FIN flag is set
            gc.setStroke(javafx.scene.paint.Color.BLACK);
            gc.strokeLine(x - width / 2, y, x + width / 2, y);
            gc.strokeLine(x, y - height / 2, x, y + height / 2);
        }

    }

    public void setFlags(boolean[] flags) {
        this.flags = flags;
    }

    @Override
    public String toString() {
        // serialize to string
        String s = "";
        s += "0,"; // type
        s += packetSize + ",";
        s += IPPoint.timeToString(time) + ",";
        s += srcIp + ",";
        s += dstIp + ",";
        s += srcPort + ",";
        s += dstPort + ",";
        for(int i = 0; i < flags.length; i++) {
            if(flags[i]) {
                s += "1,";
            } else {
                s += "0,";
            }
        }

        return s;
    }

    // deserialize from string
    public static TCPPoint fromString(String string) {
        String[] parts = string.split(",");
        TCPPoint tcpPoint = new TCPPoint(Integer.parseInt(parts[1]), IPPoint.timeFromString(parts[2]), parts[3], parts[4], Integer.parseInt(parts[5]), Integer.parseInt(parts[6]));

        boolean[] flags = new boolean[6];
        for(int i = 0; i < flags.length; i++) {
            if(parts[7 + i].equals("1")) {
                flags[i] = true;
            } else {
                flags[i] = false;
            }
        }
        tcpPoint.setFlags(flags);
        return tcpPoint;
    }

}