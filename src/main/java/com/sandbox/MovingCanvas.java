package com.sandbox;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MovingCanvas extends Application {
    private static final int CANVAS_WIDTH = 400;
    private static final int CANVAS_HEIGHT = 200;
    private static final int DATA_SHIFT_AMOUNT = 2; // Adjust this value as needed

    private Canvas canvas;
    private GraphicsContext gc;
    private double dataPosition = 0; // Current position of the data

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        gc = canvas.getGraphicsContext2D();

        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root);

        primaryStage.setTitle("Moving Canvas Example");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Create a timeline for continuously shifting the data to the left
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.millis(16.6), event -> moveDataLeft())
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void moveDataLeft() {
        // Clear the canvas
        gc.clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

        // Simulate drawing new data on the right side
        // In a real application, you would replace this with your actual data drawing code
        gc.fillRect(dataPosition, 0, 10, CANVAS_HEIGHT);

        // Shift the data position to the left
        dataPosition -= DATA_SHIFT_AMOUNT;

        // Check if the data has moved completely off the canvas
        if (dataPosition < -10) {
            dataPosition = CANVAS_WIDTH; // Reset to the right edge
        }
    }
}
