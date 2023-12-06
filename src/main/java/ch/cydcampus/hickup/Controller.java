package ch.cydcampus.hickup;

import java.io.FileNotFoundException;

import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapNativeException;

import ch.cydcampus.hickup.model.AbstractionModule;
import ch.cydcampus.hickup.model.CombinationRule;
import ch.cydcampus.hickup.model.DataBaseSource;
import ch.cydcampus.hickup.model.DataModel;
import ch.cydcampus.hickup.model.DataSource;
import ch.cydcampus.hickup.model.DoubleInterfaceSource;
import ch.cydcampus.hickup.model.FileSource;
import ch.cydcampus.hickup.model.NetworkCaptureSource;
import ch.cydcampus.hickup.model.SimpleCombinationRule;
import ch.cydcampus.hickup.view.View;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.util.Duration;

public class Controller {

    
    private final View view;
    private final DataModel dataModel;
    private Timeline timeline;
    private AbstractionModule abstractionModule;
    private DataSource dataSource;
    private CombinationRule combinationRule;

    public Controller(View view, DataModel model) {
        this.view = view;
        this.dataModel = model;
    }

    public void startApplication() throws PcapNativeException, NotOpenException {
        // Set up the UI components
        view.setController(this, dataModel);
    }

    private void startPeriodicUpdate() {
        // Set up a Timeline for periodic updates
        timeline = new Timeline(
            new KeyFrame(Duration.millis(16), event -> {
                view.updateTimelineView();
            })
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void stopPeriodicUpdate() {
        timeline.stop();
    }

    public void loadDataSource(String description, String[] params, String combinationRuleDescription) throws PcapNativeException, NotOpenException, FileNotFoundException {
        
        int sizeThreshold = 100;
        boolean playingMode = false;
        switch(description) {
            case "Network Capture":
                this.dataSource = new NetworkCaptureSource(params[0]);
                sizeThreshold = Integer.parseInt(params[1]);
                playingMode = true;
                break;
            case "File":
                this.dataSource = new FileSource(params[0]);
                sizeThreshold = Integer.parseInt(params[1]);
                break;
            case "Double Interface":
                this.dataSource = new DoubleInterfaceSource(params[0], params[1]);
                sizeThreshold = Integer.parseInt(params[2]);
                playingMode = true;
                break;
            case "Database":
                this.dataSource = new DataBaseSource(params[0], Integer.parseInt(params[1]), params[2], params[3], params[4], params[5], params[7], params[8], params[9]);
                System.out.println("Database source created");
                sizeThreshold = Integer.parseInt(params[6]);
                break;
            default:
                System.out.println("Error: Unknown data source description");
                return;
        }

        switch(combinationRuleDescription) {
            case "Simple":
                this.combinationRule = new SimpleCombinationRule();
                break;
            default:
                System.out.println("Error: Unknown combination rule");
                return;
        }

        // load data
        abstractionModule = new AbstractionModule(dataModel, dataSource, combinationRule);
        abstractionModule.setSizeThreshold(sizeThreshold);
        abstractionModule.start();

        // Switch to the main view
        view.switchToMainView(playingMode);

        // Start the periodic update
        this.startPeriodicUpdate();

    }

    public void showMenuScene() {
        stopPeriodicUpdate();
        abstractionModule.stopThread();
        dataModel.clear();
        view.switchToMenuScene();
    }

}
