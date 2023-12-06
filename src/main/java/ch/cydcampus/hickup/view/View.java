package ch.cydcampus.hickup.view;

import ch.cydcampus.hickup.Controller;
import ch.cydcampus.hickup.model.DataModel;
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
