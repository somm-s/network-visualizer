package com.hickup.points;

import java.sql.Timestamp;

import javafx.scene.canvas.GraphicsContext;

public class AnyPoint extends IPPoint {
    public AnyPoint(int packetSize, Timestamp time, boolean outgoing, String ip) {
        super(packetSize, time, outgoing, ip);
    }

    @Override
    public void draw(GraphicsContext gc, double x, double y, double width, double height) {
        // draw triangle with (x, y) in the center
        gc.fillPolygon(new double[] {x - width / 2, x + width / 2, x}, 
        new double[] {y - height / 2, y - height / 2, y + height / 2}, 3);
    }
}