package com.burstmeter;

import java.sql.Time;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class View {
    private TextField filterTextField;
    private Button applyFilterButton;
    private Controller controller;
    private TimelineCanvas canvas;

    public void setController(Controller controller, Stage primaryStage) {
        this.controller = controller;
        initializeUI(primaryStage);
    }

    private void initializeUI(Stage primaryStage) {
        // Initialize canvas
        canvas = new TimelineCanvas();
        Pane canvasPane = new Pane(canvas);
        canvas.widthProperty().bind(canvasPane.widthProperty());
        canvas.heightProperty().bind(canvasPane.heightProperty());


        // create the root pane
        HBox root = new HBox();
        VBox controlPane = new VBox();

        // initialize the control pane
        HBox.setHgrow(canvasPane, Priority.ALWAYS);
        HBox.setHgrow(controlPane, Priority.NEVER);
        root.getChildren().addAll(canvasPane, controlPane);

        // create the scene
        Scene scene = new Scene(root, 2000, 600);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        // set the scene
        primaryStage.setScene(scene);


        // Code to create and configure UI elements
        // filterTextField = new TextField();
        // applyFilterButton = new Button("Apply Filter");

        // // Set up event handlers
        // applyFilterButton.setOnAction(event -> {
        //     // Notify the controller when the Apply Filter button is clicked
        //     // controller.onApplyFilterButtonClick(filterTextField.getText());
        // });

        // Add UI elements to the layout
        // ...
    }

    public void updateCanvas() {
        canvas.draw();
    }


}
