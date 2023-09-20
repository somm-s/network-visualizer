package com.hickup;

import java.util.ArrayList;
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
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MainApplication extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    LinkedList<IPPoint> data = new LinkedList<IPPoint>();

    @Override
    public void start(Stage stage) throws Exception {

        // Create a service with a PacketCaptureTask
        PacketCaptureService service = new PacketCaptureService();
        startCapture(service);

        // Create a canvas to draw the data
        initializeCanvas(stage, service);

    }

    void initializeCanvas(Stage stage, PacketCaptureService service) {
        VBox root = new VBox();
        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.show();

        // Create a list of canvas panes
        List<Pane> canvasPanes = new ArrayList<Pane>();
        for(int i = 0; i < 1; i++) {
            canvasPanes.add(createCanvasPane());
            VBox.setVgrow(canvasPanes.get(i), javafx.scene.layout.Priority.ALWAYS);
        }
        root.getChildren().addAll(canvasPanes);

        // Create a textfield to enter the berkley packet filter string
        TextField filterTextField = new TextField();
        filterTextField.setPromptText("Enter Berkley Packet Filter String");
        root.getChildren().add(filterTextField);
        
        service.filterProperty().bind(filterTextField.textProperty());
        filterTextField.setOnAction(event -> {
            String filterString = filterTextField.getText();
            service.restart();
            System.out.println("Restart service: " + filterString);
        });

        // Create a timeline for continuously shifting the data to the left
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.millis(16), event -> {
                for(Pane canvasPane : canvasPanes) {
                    drawScene(canvasPane, data);
                }
            })
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
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

    private void startCapture(PacketCaptureService service) {
        

        service.setFilter("ip");
        service.setNetworkInterfaceName("wlp0s20f3");
        service.setReceiverIP("192.168.200.29");

        // Use the ObjectProperty in PacketCaptureService to bind the data to the canvas
        service.capturedDataProperty().addListener((obs, oldData, newData) -> {
            if(newData != null) {
                data.add(newData);
            }
        });

        service.start();
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


        // draw x axis
        g.setStroke(Color.web("#000000"));
        g.strokeLine(0, height/2, width, height/2);

        // use iterator to iterate through linked list:
        Iterator<IPPoint> it = data.iterator();
        while(it.hasNext()) {
            IPPoint d = it.next();
            long time = d.time.getTime();
            long current = System.currentTimeMillis();

            double val = d.packetSize;
            if(d.packetSize > 0) {
                val = Math.log(d.packetSize) - 2;
            }
            
            // if width over a certain threshold, keep speed of data constant
            double x = width - (current - time) * width / 4000;
            if(width > 1000) {
                x = width - (current - d.time.getTime()) * 1000 / 4000;
            }

            if(x < 0) {
                it.remove();
                continue;
            }
            double y;
            if(d.outgoing) {
                // g.setFill(Color.web("#FF9770"));
                // set color randomly according to datapoints ip address' hashcode
                g.setFill(Color.hsb(Math.abs(d.ip.hashCode()) % 360, 1, 1));
                y = height / 2 + 4 + (height / 2) * val / maxVal;

            } else {
                // g.setFill(Color.web("#70D6FF"));
                // use same color as sent packets but with a slight tweak
                g.setFill(Color.hsb(Math.abs(d.ip.hashCode()) % 360, 1, 1, 1));
                y = height / 2 - (4 + (height / 2) * val / maxVal);
            }
            d.draw(g, x, y, 8, 8);
        }
    }
}
