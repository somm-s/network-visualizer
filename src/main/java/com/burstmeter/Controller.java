package com.burstmeter;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Controller {
    
    private final View view;
    private final Model model;

    public Controller(View view, Model model) {
        
        this.view = view;
        this.model = model;

    }

    public void startPeriodicUpdate() {
        // Set up a Timeline for periodic updates
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.millis(6), event -> {
                Platform.runLater(() -> {
                    view.updateCanvas();
                });
            })
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

}
