package com.lockedshields;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.hickup.points.IPPoint;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MainApplication extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    LinkedList<IPPoint> data = new LinkedList<>();
    double timeInterval = 3000; //3000; // timeinterval shown in canvas in nanoseconds
    double startTime = 0; // start time of the data in seconds
    double startXDrag = 0; // start x coordinate of the drag
    double startTimeOnDrag = 0; // start time of the data on drag
    double firstPacketTime = 0; // time of the first packet in the data
    double lastPacketTime = 0; // time of the last packet in the data

    @Override
    public void start(Stage stage) throws Exception {
        Pane root = createCanvasPane();
        Scene scene = new Scene(root, 1000, 600);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.show();

        // initialize data from pcap
        // PcapLoader pcapLoader = new PcapLoader("kolka-220420-00002080.pcap.gz");
        // pcapLoader.loadDataFromPcap(data);
        
        IPPoint[] arrayData = IPPoint.readFromFiles("/home/lab/Documents/networking/ls22/0", "94.246.231.95");
        // sort arrayData by time
        Arrays.sort(arrayData, (a, b) -> a.time.compareTo(b.time));
        data = new LinkedList<>(Arrays.asList(arrayData));


        // use the first datapoint to adjust the start time
        startTime = data.getFirst().time.getTime();
        firstPacketTime = data.getFirst().time.getTime();
        lastPacketTime = data.getLast().time.getTime();
        printStats(data);


        // add a scroll listener to the canvas
        Canvas canvas = (Canvas) root.getChildren().get(0);
        canvas.setOnScroll(event -> {
            double lastInterval = timeInterval;
            if(event.getDeltaY() > 0) {
                timeInterval = timeInterval * 1.111111;
            } else if (event.getDeltaY() < 0) {
                timeInterval = timeInterval * 0.9;
            }

    

            // adjust the start time so that the middle of the time interval stays the same
            startTime += (lastInterval - timeInterval) / 2;
            System.out.println("time interval: " + timeInterval + "delta " + event.getDeltaY());
        });

        // add listener for drag and drop to control the start time of the data shown in the canvas
        // dragging left or right changes the start time of the data shown in the canvas
        canvas.setOnMousePressed(event -> {
            startXDrag = event.getX();
            startTimeOnDrag = startTime;
            System.out.println("drag entered" + startXDrag);
        });

        canvas.setOnMouseDragged(event -> {
            // calculate delta
            double delta = event.getX() - startXDrag;
            // adjust start time
            startTime = startTimeOnDrag - delta * timeInterval / canvas.getWidth();
        });

        // Start drawing animation
        // Create a timeline for continuously shifting the data to the left
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.millis(16), event -> {
                drawScene(root, data);
            })
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    // print statistics about the data
    private void printStats(LinkedList<IPPoint> data) {
        System.out.println("Number of packets: " + data.size());
        System.out.println("First packet: " + data.getFirst());
        System.out.println("Last packet: " + data.getLast());

        // calculate distinct ip addresses among src and dest using a set
        HashMap<String, Integer> ipMap = new HashMap<String, Integer>();
        HashMap<String, Integer> srcIpMap = new HashMap<String, Integer>();
        HashMap<String, Integer> dstIpMap = new HashMap<String, Integer>();

        Iterator<IPPoint> it = data.iterator();
        while(it.hasNext()) {
            IPPoint d = it.next();
            // construct key by concatenating src and dst ip addresses
            String key = d.srcIp + " to " + d.dstIp;

            // if key is not in the map, add it with value 1
            if(!ipMap.containsKey(key)) {
                ipMap.put(key, 1);
            } else {
                // if key is in the map, increment its value
                ipMap.put(key, ipMap.get(key) + 1);
            }

            // do the same for src and dst ip addresses
            if(!srcIpMap.containsKey(d.srcIp)) {
                srcIpMap.put(d.srcIp, 1);
            } else {
                srcIpMap.put(d.srcIp, srcIpMap.get(d.srcIp) + 1);
            }

            if(!dstIpMap.containsKey(d.dstIp)) {
                dstIpMap.put(d.dstIp, 1);
            } else {
                dstIpMap.put(d.dstIp, dstIpMap.get(d.dstIp) + 1);
            }
        }

        System.out.println("Number of distinct src ip addresses: " + srcIpMap.size());
        System.out.println("Number of distinct dst ip addresses: " + dstIpMap.size());

        System.out.println("Number of distinct host-to-host connections: " + ipMap.size());

        // print out the top 10 most frequent host-to-host connections
        List<String> top10 = new ArrayList<String>(ipMap.keySet());
        top10.sort((a, b) -> ipMap.get(b) - ipMap.get(a));
        for(int i = 0; i < Math.min(100, srcIpMap.size()); i++) {
            System.out.println(top10.get(i) + " " + ipMap.get(top10.get(i)));
        }
    }

    private Pane createCanvasPane() {
        Pane canvasPane = new Pane();
        Canvas canvas = new Canvas(800, 600); // Set initial canvas size
        canvas.widthProperty().bind(canvasPane.widthProperty());
        canvas.heightProperty().bind(canvasPane.heightProperty());
        canvasPane.getChildren().add(canvas);
        drawScene(canvasPane, data);
        return canvasPane;
    }
    
    // update drawing of time series data. Current time is on the right of the canvas
    private void drawScene(Pane canvasPane, LinkedList<IPPoint> data) {

        Canvas canvas = (Canvas) canvasPane.getChildren().get(0);
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        g.setFill(Color.web("#AAAAAA"));
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        double width = canvas.getWidth();
        double height = canvas.getHeight();
        double maxVal = 11;


        // draw bar at the bottom indicating the time interval shown in the canvas
        g.setStroke(Color.web("#000000"));
        g.strokeLine(0, height - 20, width, height - 20);
        // use dark gray
        g.setFill(Color.web("#444444"));
        // fill a rect corresponding to the current window.
        g.fillRect(width * (startTime - firstPacketTime) / (lastPacketTime - firstPacketTime), height - 20, width * timeInterval / (lastPacketTime - firstPacketTime), 20);


        // on top of bar, write at 5 positions the time corresponding to the position. Transform the time in nanoseconds to utf format
        g.setFill(Color.web("#000000"));

        for(int i = 0; i < 5; i++) {
            // have some slack at the sides
            double slack = timeInterval * 0.1;
            String time = new java.text.SimpleDateFormat("dd-MM-HH:mm:ss:SSS").format(new java.util.Date((long) (startTime + slack + i * (timeInterval - 2 * slack) / 4)));
            // draw a tick mark on top of the time interval bar
            double widthSlack = 0.1 * width;
            g.strokeLine(widthSlack + i * ((width - 2 * widthSlack) / 4), height - 20, widthSlack + i * ((width - 2 * widthSlack) / 4), height - 25);
            // write the time
            g.fillText(time, widthSlack + i * ((width - 2 * widthSlack) / 4) - 90, height - 30);
        }


        // draw x axis
        g.setStroke(Color.web("#000000"));
        g.strokeLine(0, height/2, width, height/2);

        // use iterator to iterate through linked list:
        Iterator<IPPoint> it = data.iterator();
        while(it.hasNext()) {

            IPPoint d = it.next();
            
            // if packet has 204.79.197.219 as source or destination remove it
            // if(!d.srcIp.equals("151.101.246.49") && !d.dstIp.equals("151.101.246.49")) {
            //     it.remove();
            //     continue;
            // }


            // check if data point is in the time interval shown in the canvas
            if(d.time.getTime() < startTime) {
                continue;
            }
            if(d.time.getTime() > startTime + timeInterval) {
                break; // data points are sorted by time, so we can break here
            }

            // calculate x and y coordinates of the data point
            double x = width * (d.time.getTime() - startTime) / timeInterval;

            double val = d.packetSize;
            if(d.packetSize > 0) {
                val = Math.log(d.packetSize) - 2;
            }

            double y;

            if(d.srcIp.equals("10.3.8.5")) {
                // g.setFill(Color.web("#FF9770"));
                // set color randomly according to datapoints ip address' hashcode
                g.setFill(Color.hsb(Math.abs(d.dstIp.hashCode()) % 360, 1, 1));
                y = height / 2 + 4 + (height / 2) * val / maxVal;

            } else {
                // g.setFill(Color.web("#70D6FF"));
                // use same color as sent packets but with a slight tweak
                g.setFill(Color.hsb(Math.abs(d.srcIp.hashCode()) % 360, 1, 1, 1));
                y = height / 2 - (4 + (height / 2) * val / maxVal);
            }
            d.draw(g, x, y, 8, 8);
        }
    }
}
