package com.sandbox;

import java.io.EOFException;
import java.util.concurrent.TimeoutException;

import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.Pcaps;
import org.pcap4j.packet.Packet;
import java.util.Date;
import java.text.SimpleDateFormat;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;


/*
 * A simple class using JavaFX to display a chart of packet sizes.
 * It captures every packet and tries to extract the packet size and displays it on the chart.
 */
public class Main extends Application {
    
    private final int CHART_DATA_CAPACITY = 1000; // Number of data points to show on the chart
    private PcapNetworkInterface networkInterface;
    private LineChart<String, Number> chart;
    private XYChart.Series<String, Number> series;
    final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss.SSS");

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        // Initialize the chart
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Time");
        yAxis.setLabel("Packet Size");
        xAxis.setAnimated(false); // axis animations are removed
        yAxis.setAnimated(false); // axis animations are removed

        chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Packet Sizes");
        chart.setAnimated(false);
        series = new XYChart.Series<>();
        series.setName("Packet Size");
        chart.getData().add(series);

        // Create the scene
        Scene scene = new Scene(chart, 800, 600);

        // Set up the network interface for packet capture
        try {
            networkInterface = Pcaps.getDevByName("wlp0s20f3");
        } catch (PcapNativeException e) {
            e.printStackTrace();
            System.exit(1);
        }
        
        // Set up packet capture
        PcapHandle handle = networkInterface.openLive(65536, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, 10);

        // Set up the packet listener
        new Thread(() -> {
            while (true) {
                try {
                    Packet packet = null;
                    try {
                        packet = handle.getNextPacketEx();
                    } catch (EOFException | TimeoutException e) {
                        e.printStackTrace();
                    }
                    if (packet != null) {
                        int packetSize = packet.length();
                        Platform.runLater(() -> updateChart(packetSize));
                    }
                } catch (PcapNativeException | NotOpenException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        // Set up and show the stage
        primaryStage.setTitle("Packet Capture App");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void updateChart(int packetSize) {
        // Remove old data points if the series exceeds the capacity
        if (series.getData().size() >= CHART_DATA_CAPACITY) {
            series.getData().remove(0);
        }
        double log_size = Math.log(packetSize);
        Date now = new Date();

        // Add data points to the chart series
        series.getData().add(new XYChart.Data<>(simpleDateFormat.format(now), log_size));

    }
}
