package com.sandbox;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import org.pcap4j.core.BpfProgram;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PacketListener;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.Pcaps;
import org.pcap4j.packet.Packet;
import org.pcap4j.util.NifSelector;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FromPcap extends Application {

    private static final String PCAP_FILE_PATH = "/home/lab/Documents/networking/local_capture_3_min.pcap";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("PCAP Packet Size Chart");

        // Create a BarChart to display packet sizes
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Packet Sizes");
        xAxis.setLabel("Time");
        yAxis.setLabel("Packet Size");

        // Parse the PCAP file and collect packet size data
        ObservableList<XYChart.Data<String, Number>> packetSizeData = parsePcapFile();

        // Create a series and add data to the chart
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Packet Sizes");
        series.setData(packetSizeData);

        // Add the series to the chart
        barChart.getData().add(series);

        Scene scene = new Scene(barChart, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Parse the PCAP file and collect packet size data.
    // The X axis are timestamps and the y axis are packet sizes.
    private ObservableList<XYChart.Data<String, Number>> parsePcapFile() {

        // Create ObservableList for the BarChart data
        ObservableList<XYChart.Data<String, Number>> data = FXCollections.observableArrayList();


        // Read the PCAP file
        try {
            PcapHandle handle = Pcaps.openOffline(PCAP_FILE_PATH);
            handle.setFilter("ip", BpfProgram.BpfCompileMode.OPTIMIZE);
            PacketListener pl = new PacketListener() {
                @Override
                public void gotPacket(Packet packet) {
                    double packetSize = Math.log(packet.length()) - 4;

                    // add timestamp including milliseconds
                    data.add(new XYChart.Data<>(String.valueOf(handle.getTimestamp().getTime()), packetSize));
                }
            };
            handle.loop(-1, pl);
        } catch (PcapNativeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NotOpenException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return data;
    }
}
