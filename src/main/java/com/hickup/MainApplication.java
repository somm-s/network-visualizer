package com.hickup;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.hickup.points.IPPoint;
import com.hickup.services.PacketCSVService;
import com.hickup.services.PacketCaptureService;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
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
        HBox root = new HBox();
        VBox vbox_left = new VBox();
        VBox vbox_right = new VBox();
        HBox.setHgrow(vbox_left, Priority.ALWAYS);
        HBox.setHgrow(vbox_right, Priority.NEVER);
        root.getChildren().addAll(vbox_left, vbox_right);
        Scene scene = new Scene(root, 1000, 600);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.show();

        // create a service with a PacketCSVTask
        PacketCSVService csvService = new PacketCSVService();

        // Create a service with a PacketCaptureTask
        PacketCaptureService service = new PacketCaptureService();
        
        // Create a canvas to draw the data
        initCaptureService(service);
        initializeCanvas(vbox_left, service);
        initializeControl(vbox_right, csvService, service);
    }

    void initializeControl(Pane root, PacketCSVService csvService, PacketCaptureService service) {
        // Create a TextField to enter the interface to listen to. Add a title before it
        Label titleLabel = new Label("HiCKUP Monitor");
        titleLabel.getStyleClass().add("title-label");
        Label interfaceLabel = new Label("Select Interface");
        interfaceLabel.getStyleClass().add("subtitle-label");
        TextField interfaceTextField = new TextField();
        interfaceTextField.setPromptText("Enter Interface Name");
        interfaceTextField.getStyleClass().add("textbox-label");
        Label observerIPLabel = new Label("Select Host as Observer");
        observerIPLabel.getStyleClass().add("subtitle-label");
        TextField observerIPTextField = new TextField();
        observerIPTextField.setPromptText("Enter Host IP");
        observerIPTextField.getStyleClass().add("textbox-label");

        // Create a button to restart the PacketCaptureService
        Button restartButton = new Button("Restart");
        restartButton.getStyleClass().add("button");
        restartButton.setOnAction(event -> {
            String interfaceName = interfaceTextField.getText();
            String observerIP = observerIPTextField.getText();
            service.setNetworkInterfaceName(interfaceName);
            service.setReceiverIP(observerIP);
            service.restart();
        });

        // Create a horizontal line to separate the two sections
        Separator separator = new Separator();
        separator.getStyleClass().add("separator");

        Label csvLabel = new Label("CSV Dump (Pkt)");
        csvLabel.getStyleClass().add("title-label");


        // Create a label and textfield to enter the file name
        Label fileNameLabel = new Label("Enter File Name");
        fileNameLabel.getStyleClass().add("subtitle-label");
        TextField fileNameTextField = new TextField();
        fileNameTextField.setPromptText("Enter File Name");
        fileNameTextField.getStyleClass().add("textbox-label");

        // Create a label and textfield to enter the berkley packet filter string
        Label filterLabel = new Label("Filter String for Dump");
        filterLabel.getStyleClass().add("subtitle-label");
        TextField filterTextField = new TextField();
        filterTextField.setPromptText("Enter BPF String");
        filterTextField.getStyleClass().add("textbox-label");

        // Create start and "stop & save" buttons in hbox
        HBox buttonBox = new HBox();
        buttonBox.getStyleClass().add("button-box");
        Button startButton = new Button("Start");
        startButton.getStyleClass().add("button");
        startButton.setOnAction(event -> {
            String fileName = fileNameTextField.getText();
            String filterString = filterTextField.getText();
            String observerIP = observerIPTextField.getText();
            String networkInterfaceName = interfaceTextField.getText();
            csvService.setReceiverIP(observerIP);
            csvService.setFileName(fileName);
            csvService.setFilter(filterString);
            csvService.setNetworkInterfaceName(networkInterfaceName);
            csvService.restart();
        });

        Button stopButton = new Button("Stop & Save");
        stopButton.getStyleClass().add("button");
        stopButton.setOnAction(event -> {
            csvService.cancel();
        });
        startButton.setMaxWidth(Double.MAX_VALUE);
        restartButton.setMaxWidth(Double.MAX_VALUE);
        buttonBox.getChildren().addAll(startButton, stopButton);
        HBox.setHgrow(startButton, Priority.ALWAYS);
        HBox.setHgrow(stopButton, Priority.ALWAYS);
        

        // Add all the elements to the root pane
        root.getChildren().addAll(titleLabel, interfaceLabel, interfaceTextField, observerIPLabel, observerIPTextField, restartButton, separator, csvLabel, fileNameLabel, fileNameTextField, filterLabel, filterTextField, buttonBox);
    }

    void initializeCanvas(Pane root, PacketCaptureService service) {


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
        service.start();
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


    private void initCaptureService(PacketCaptureService service) {
        

        service.setFilter("ip");
        service.setNetworkInterfaceName("wlp0s20f3");
        service.setReceiverIP("192.168.200.29");

        // Use the ObjectProperty in PacketCaptureService to bind the data to the canvas
        service.capturedDataProperty().addListener((obs, oldData, newData) -> {
            if(newData != null) {
                data.add(newData);
            }
        });

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
            double x = width - (current - time) * width / 5000;
            if(width > 1000) {
                x = width - (current - d.time.getTime()) * 1000 / 5000;
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
