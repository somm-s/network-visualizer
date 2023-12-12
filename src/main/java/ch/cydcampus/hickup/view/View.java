package ch.cydcampus.hickup.view;

import ch.cydcampus.hickup.Controller;
import ch.cydcampus.hickup.model.DataModel;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class View {
    private Stage primaryStage;
    private TimelineScene timelineScene;
    private DataSourceSelectionScene dataSourceSelectionScene;

    private Controller controller;
    private DataModel model;

    public View(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void createSimplePlot(double[] x, double[] y) {
        
        // Create X and Y axes
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();

        // Create a line chart
        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Simple Plot");

        // Create a series and add data
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        for (int i = 0; i < x.length; i++) {
            series.getData().add(new XYChart.Data<>(x[i], y[i]));
        }

        // Add series to the chart
        lineChart.getData().add(series);

        // Create the main scene with the chart
        Scene mainScene = new Scene(lineChart, 2000, 1000);

        // Set the scene to the primary stage
        primaryStage.setScene(mainScene);
        primaryStage.show();
    }

    public void createSimpleScatterPlot(double[] data) {
        double[] indices = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            indices[i] = i;
        }

        createSimpleScatterPlot(indices, data);
    }

    public void createSimpleScatterPlot(double[] x, double[] y) {
        // Create X and Y axes
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();

        // Create a scatter chart
        ScatterChart<Number, Number> scatterChart = new ScatterChart<>(xAxis, yAxis);
        scatterChart.setTitle("Simple Scatter Plot");

        // Create a series and add data
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        for (int i = 0; i < x.length; i++) {
            series.getData().add(new XYChart.Data<>(x[i], y[i]));
        }

        // Add series to the chart
        scatterChart.getData().add(series);

        // Create the main scene with the chart
        Scene mainScene = new Scene(scatterChart, 2000, 1000);

        // Set the scene to the primary stage
        primaryStage.setScene(mainScene);
        primaryStage.show();
    }

    public void createSimpleHistogram(double[] data, int numBuckets) {
        // Create a histogram chart
        BarChart<String, Number> histogramChart = createHistogramChart(data, numBuckets);
        
        // Create the main scene with the chart
        Scene mainScene = new Scene(histogramChart, 2000, 1000);

        // Set the scene to the primary stage
        primaryStage.setScene(mainScene);
        primaryStage.show();
    }

    private BarChart<String, Number> createHistogramChart(double[] data, int numBuckets) {
        // Create a histogram series
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        
        // Calculate bin width
        double max = getMax(data);
        double min = getMin(data);
        double binWidth = (max - min) / numBuckets;

        // Populate histogram series
        for (int i = 0; i < numBuckets; i++) {
            double binStart = min + i * binWidth;
            double binEnd = binStart + binWidth;
            int count = getCountInBin(data, binStart, binEnd);
            series.getData().add(new XYChart.Data<>(String.format("%.2f-%.2f", binStart, binEnd), count));
        }

        // Create X and Y axes
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();

        // Create a histogram chart
        BarChart<String, Number> histogramChart = new BarChart<>(xAxis, yAxis);
        histogramChart.setTitle("Simple Histogram");

        // Add histogram series to the chart
        histogramChart.getData().add(series);

        return histogramChart;
    }

    private double getMax(double[] data) {
        double max = data[0];
        for (double value : data) {
            max = Math.max(max, value);
        }
        return max;
    }

    private double getMin(double[] data) {
        double min = data[0];
        for (double value : data) {
            min = Math.min(min, value);
        }
        return min;
    }

    private int getCountInBin(double[] data, double binStart, double binEnd) {
        int count = 0;
        for (double value : data) {
            if (value >= binStart && value < binEnd) {
                count++;
            }
        }
        return count;
    }

    public void setController(Controller controller, DataModel model) {
        this.controller = controller;
        this.model = model;
        initializeUI();
        primaryStage.show();
    }

    private void initializeUI() {
        // Initialize scenes
        dataSourceSelectionScene = new DataSourceSelectionScene(new Pane(), 705, 300, controller);
        primaryStage.setScene(dataSourceSelectionScene);
    }

    public void updateTimelineView() {
        this.timelineScene.updateTimelineView();
    }

    public void switchToMainView(boolean playingMode) {
        timelineScene = new TimelineScene(new Pane(), 2400, 1000, controller, model);
        timelineScene.setPlayingMode(playingMode);
        primaryStage.setScene(timelineScene);
    }

    public void switchToDataSourceSelectionView() {
        primaryStage.setScene(dataSourceSelectionScene);
    }

    public void switchToMenuScene() {
        primaryStage.setScene(dataSourceSelectionScene);
    }


}
