package ch.cydcampus.hickup;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.util.Duration;

public class Controller {
    
    private final View view;
    // private final Model model;

    public Controller(View view) {
        
        this.view = view;
        // this.model = model;

    }

    public void startPeriodicUpdate() {
        // Set up a Timeline for periodic updates
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.millis(16), event -> {
                view.updateCanvas();
            })
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

}
