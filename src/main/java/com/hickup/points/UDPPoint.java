package com.hickup.points;

import java.sql.Timestamp;

import javafx.scene.canvas.GraphicsContext;

public class UDPPoint extends IPPoint {

    public UDPPoint(int packetSize, Timestamp time, boolean outgoing, String ip) {
        super(packetSize, time, outgoing, ip);
    }

    @Override
    public void draw(GraphicsContext gc, double x, double y, double width, double height) {
        gc.fillRect(x - (width - 1) / 2, y - (height - 1) / 2, width - 1, height - 1);
    }
    
}
