package com.burstmeter;
import java.util.HashMap;
import com.hickup.points.IPPoint;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class TimelineCanvas extends Canvas {

    double maxVal = 24;
    long timeInterval = 10000000; // 10 seconds

    String observedHost = "192.168.200.29";

    public TimelineCanvas() {
        super();
    }

    private void drawBackground(GraphicsContext g) {
        g.clearRect(0, 0, getWidth(), getHeight());
        g.setFill(Color.web("#222222"));
        g.fillRect(0, 0, getWidth(), getHeight());

        // draw x axis
        g.setStroke(Color.web("#000000"));
        g.strokeLine(0, getHeight()/2, getWidth(), getHeight()/2);        
    }

    private void drawData(GraphicsContext g) {
        int count = 0;

        // get data
        HashMap<String, Burst> connections = Model.getInstance().connections;
        // System.out.println(connections.size() + " connections");

        // go over all connections
        for(Burst b : connections.values()) {
            // System.out.println("----\n" + b.getConnectionIdentifier());
            // System.out.println(b);

            Burst last = null;
            // go over all bursts
            while(b != null) {
                // skip if end time is before current time - time interval

                // get current time in microseconds:
                long current = System.currentTimeMillis() * 1000;
                
                if(IPPoint.getMicroseconds(b.getEndTime()) < current - timeInterval - 1000000) {
                    if(last != null) {
                        // System.out.println("removing burst...");
                        last.setNext(null);
                    } else {
                        // System.out.println("removing connection...");
                        // connections.remove(b.getConnectionIdentifier()); TODO: use iterator for this...
                    }
                    // System.out.println("Connections: " + connections.size());
                    break;
                }

                count++;

                // draw burst
                drawBurst(g, b);

                // go to next burst
                last = b;
                b = b.next;
            }
        }
        // System.out.println(count + " bursts");
    }

    private void drawBurst(GraphicsContext g, Burst b) {

        // calculate x and y coordinates of the data point
        long current = System.currentTimeMillis() * 1000;
        double xFraction = (current - IPPoint.getMicroseconds(b.getAvgTime())) / (double) timeInterval;
        double x = getWidth() * (1 - xFraction);

        double y;
        Color c = null;
        if(b.getSrcHost().equals(observedHost)) {

            // g.setFill(Color.web("#FF9770"));
            // set color randomly according to datapoints ip address' hashcode
            y = getHeight() / 2 + getHeight() * Math.log(b.getBytes()) / maxVal / 2;
            
            // y = getHeight() / 2 + (getHeight() * b.getBytes()) / maxVal / 2;

            g.setFill(Color.hsb(Math.abs(b.getDstHost().hashCode()) % 360, 1, 1));
            c = Color.hsb(Math.abs(b.getDstHost().hashCode()) % 360, 1, 1);
        } else {
            // g.setFill(Color.web("#70D6FF"));
            // use same color as sent packets but with a slight tweak
            y = getHeight() / 2 - getHeight() * Math.log(b.getBytes()) / maxVal / 2;
            // y = getHeight() / 2 - (getHeight() * b.getBytes()) / maxVal / 2;

            g.setFill(Color.hsb(Math.abs(b.getSrcHost().hashCode()) % 360, 1, 1, 1));
            c = Color.hsb(Math.abs(b.getSrcHost().hashCode()) % 360, 1, 1, 1);
        }

        // if the burst is still being filled, draw every point of it instead of just the average
        if(IPPoint.getMicroseconds(b.getEndTime()) > current - 3000000) {
            for(IPPoint p : b.getIpPoints()) {
                double xFraction2 = (current - IPPoint.getMicroseconds(p.time)) / (double) timeInterval;
                double x2 = getWidth() * (1 - xFraction2);
                double y2;
                if(b.getSrcHost().equals(observedHost)) {
                    y2 = getHeight() / 2 + getHeight() * Math.log(p.packetSize) / maxVal / 2;
                } else {
                    y2 = getHeight() / 2 - getHeight() * Math.log(p.packetSize) / maxVal / 2;
                }
                g.fillOval(x2 - 2, y2 - 2, 4, 4);
            }
        } else {
            // draw a circle if tcp
            if(b.getProtocol() == IPPoint.TCP_PROTOCOL) {
                g.fillOval(x - 5, y - 5, 10, 10);
            } else {
                g.fillRect(x - 5, y - 5, 10, 10);
            }
    
            // // if last burst not null, draw a line to it if time difference is less than Model.burstTimeout
            // if(b.prev != null && (b.prev.getX() != 0 && b.prev.getY() != 0)) {
            //     g.setStroke(c);
            //     // System.out.println(lastX + " " + lastY + " " + x + " " + y);
            //     g.strokeLine(b.prev.getX(), b.prev.getY(), x, y);
            // }
            // b.setX(x);
            // b.setY(y);
        }


    }

    public void draw() {
        GraphicsContext gc = getGraphicsContext2D();
        drawBackground(gc);
        drawData(gc);
    }
}
