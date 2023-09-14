package com.hickup;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Test extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Create a Pane as the outer rectangle (container)
        Pane outerPane = new Pane();
        outerPane.setLayoutX(50);
        outerPane.setLayoutY(50);
        outerPane.setPrefWidth(200);
        outerPane.setPrefHeight(100);
        outerPane.setStyle("-fx-background-color: lightgray;");

        // Create the inner rectangle (moving element inside the outer pane)
        Rectangle innerRectangle = new Rectangle(50, 50, 50, 50);
        innerRectangle.setFill(Color.BLUE);

        // Create a Timeline with a fixed frame rate of 60 fps
        Duration frameDuration = Duration.millis(16.67); // 1000 ms / 60 fps ≈ 16.67 ms
        Timeline timeline = new Timeline(new KeyFrame(frameDuration, event -> {
            // Update the animation here
            double newX = innerRectangle.getTranslateX() + 1; // Adjust as needed
            innerRectangle.setTranslateX(newX);
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        // Add the inner rectangle to the outer pane
        outerPane.getChildren().add(innerRectangle);

        // Create a Pane to hold the outer pane and inner rectangle
        Pane root = new Pane(outerPane);

        // Create a Scene
        Scene scene = new Scene(root, 300, 200);

        // Set up the stage
        primaryStage.setTitle("60 FPS Animated Rectangles Example");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
