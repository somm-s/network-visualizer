package ch.cydcampus.hickup;

import ch.cydcampus.hickup.view.View;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.util.Duration;

public class Controller {

    public static final String OBSERVED_NETWORK_PREFIX = "192.168.200";
    
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
                view.updateView();
            })
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

}
