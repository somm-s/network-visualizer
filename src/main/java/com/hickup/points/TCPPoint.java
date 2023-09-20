package com.hickup.points;

import java.sql.Timestamp;

import javafx.scene.canvas.GraphicsContext;

public class TCPPoint extends IPPoint {

    boolean[] flags = new boolean[6]; // FIN, SYN, RST, PSH, ACK, URG

    public TCPPoint(int packetSize, Timestamp time, boolean outgoing, String ip) {
        super(packetSize, time, outgoing, ip);
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

}