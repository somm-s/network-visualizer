package com.lockedshields;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;

import com.hickup.points.IPPoint;
import com.lockedshields.DataBuffer.DataChangeListener;

import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class TimelineCanvas extends Canvas implements DataChangeListener {

    double maxVal = 11;
    DataBuffer dataBuffer;


    public TimelineCanvas(DataBuffer dataBuffer) {
        super();
        this.dataBuffer = dataBuffer;
        // register event handlers
        dataBuffer.addListener(this);
    }

    private void drawBackground(GraphicsContext g) {
        g.clearRect(0, 0, getWidth(), getHeight());
        g.setFill(Color.web("#222222"));
        g.fillRect(0, 0, getWidth(), getHeight());

        if(dataBuffer.dataLoaded) {


            // draw bar at the bottom indicating the time interval shown in the canvas
            g.setStroke(Color.web("#CCCCCC"));
            g.strokeLine(0, getHeight() - 20, getWidth(), getHeight() - 20);
            // use dark gray
            g.setFill(Color.web("#AAAAAA"));
            // fill a rect corresponding to the current window.
            g.fillRect(getWidth() * (dataBuffer.startTime - dataBuffer.firstPacketTime) / (dataBuffer.lastPacketTime - dataBuffer.firstPacketTime), 
                getHeight() - 20, getWidth() * dataBuffer.timeInterval / (dataBuffer.lastPacketTime - dataBuffer.firstPacketTime), 20);

            // on top of bar, write at 5 positions the time corresponding to the position. Transform the time in nanoseconds to utf format
            g.setFill(Color.web("#FFFFFF"));

            for(int i = 0; i < 5; i++) {
                // have some slack at the sides
                double slack = dataBuffer.timeInterval * 0.1;
                String time = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS").format(IPPoint.microToInstant((long) (dataBuffer.startTime + slack + i * (dataBuffer.timeInterval - 2 * slack) / 4)).atZone(ZoneOffset.UTC));
                // draw a tick mark on top of the time interval bar
                double widthSlack = 0.1 * getWidth();
                g.strokeLine(widthSlack + i * ((getWidth() - 2 * widthSlack) / 4), getHeight() - 20, widthSlack + i * ((getWidth() - 2 * widthSlack) / 4), getHeight() - 25);
                // write the time
                g.fillText(time, widthSlack + i * ((getWidth() - 2 * widthSlack) / 4) - 90, getHeight() - 30);
            }
        }

        // draw x axis
        g.setStroke(Color.web("#000000"));
        g.strokeLine(0, getHeight()/2, getWidth(), getHeight()/2);        
    }

    private void drawData(GraphicsContext g) {
        Iterator<IPPoint> it = dataBuffer.iterator();
        IPPoint last = null;
        while(it.hasNext()) {
            IPPoint d = it.next();

            if(last != null) {
                // check if data is sorted
                if(d.getMicroseconds() < last.getMicroseconds()) {
                    System.out.println("Data not sorted");
                }
            }

            // calculate x and y coordinates of the data point
            double x = (d.getMicroseconds() - dataBuffer.startTime) * (getWidth() / dataBuffer.timeInterval);

            double y;

            if(d.srcIp.equals(dataBuffer.getObservedHost())) {
                // g.setFill(Color.web("#FF9770"));
                // set color randomly according to datapoints ip address' hashcode
                g.setFill(Color.hsb(Math.abs(d.dstIp.hashCode()) % 360, 1, 1));
                y = getHeight() / 2 + 4 + (getHeight() / 2) * d.val / maxVal;

            } else {
                // g.setFill(Color.web("#70D6FF"));
                // use same color as sent packets but with a slight tweak
                g.setFill(Color.hsb(Math.abs(d.srcIp.hashCode()) % 360, 1, 1, 1));
                y = getHeight() / 2 - (4 + (getHeight() / 2) * d.val / maxVal);
            }
            d.draw(g, x, y, 8, 8);
            d.setX(x);
            d.setY(y);
            last = d;
        }

    }

    public void draw() {
        GraphicsContext gc = getGraphicsContext2D();
        drawBackground(gc);
        drawData(gc);
    }

    @Override
    public void onDataChanged() {
        // draw data. Use application thread.
        Platform.runLater(() -> draw());
    }

}
