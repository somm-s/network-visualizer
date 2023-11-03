package com.lockedshields;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LSApplication extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        // create the data buffer
        DataBuffer dataBuffer = new DataBuffer();

        // create the custom canvas
        TimelineCanvas canvas = new TimelineCanvas(dataBuffer);
        Pane canvasPane = new Pane(canvas);
        canvas.widthProperty().bind(canvasPane.widthProperty());
        canvas.heightProperty().bind(canvasPane.heightProperty());

        // create the UI event handler
        UIEventHandler uiEventHandler = new UIEventHandler(dataBuffer, canvas);
        canvas.setOnMouseEntered(uiEventHandler);
        canvas.setOnMouseExited(uiEventHandler);
        canvas.setOnMousePressed(uiEventHandler);
        canvas.setOnMouseDragged(uiEventHandler);
        canvas.setOnScroll(uiEventHandler);
        canvas.widthProperty().addListener((obs, oldWidth, newWidth) -> canvas.draw());
        canvas.heightProperty().addListener((obs, oldHeight, newHeight) -> canvas.draw());

        // create the UI button handler
        UIButtonHandler uiButtonHandler = new UIButtonHandler(dataBuffer);

        // create the root pane
        HBox root = new HBox();
        VBox controlPane = new VBox();

        // initialize the control pane
        initializeControl(uiEventHandler, uiButtonHandler, controlPane);
        HBox.setHgrow(canvasPane, Priority.ALWAYS);
        HBox.setHgrow(controlPane, Priority.NEVER);
        root.getChildren().addAll(canvasPane, controlPane);

        // create the scene
        Scene scene = new Scene(root, 2000, 600);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        // set the scene
        primaryStage.setScene(scene);
        primaryStage.show();
    }
 
    private void initializeControl(UIEventHandler uiEventHandler, UIButtonHandler uiButtonHandler, VBox controlPane) {

        // Create a TextField to enter the interface to listen to. Add a title before it
        Label titleLabel = new Label("LS Visualizer");
        titleLabel.getStyleClass().add("title-label");
        Label startTimeLabel = new Label("Select Start Time");
        startTimeLabel.getStyleClass().add("subtitle-label");
        TextField startTimeTextField = new TextField();
        startTimeTextField.setPromptText("yyyy-MM-dd HH:mm:ss.SSSSSS");
        startTimeTextField.getStyleClass().add("textbox-label");
        startTimeTextField.setText("");
        uiButtonHandler.startTimeProperty().bind(startTimeTextField.textProperty());

        Label endTimeLabel = new Label("Select End Time");
        endTimeLabel.getStyleClass().add("subtitle-label");
        TextField endTimeTextField = new TextField();
        endTimeTextField.setPromptText("yyyy-MM-dd HH:mm:ss.SSSSSS");
        endTimeTextField.getStyleClass().add("textbox-label");
        endTimeTextField.setText("");
        uiButtonHandler.endTimeProperty().bind(endTimeTextField.textProperty());

        // Create a label and textfield to enter src host IP
        Label observedHostLabel = new Label("Enter Observed Host");
        observedHostLabel.getStyleClass().add("subtitle-label");
        TextField observedHostTextField = new TextField();
        observedHostTextField.setPromptText("192.168.0.0");
        observedHostTextField.getStyleClass().add("textbox-label");
        observedHostTextField.setText("");
        uiButtonHandler.observedHostProperty().bind(observedHostTextField.textProperty());

        // Create a label and textfield to enter dst host IP
        Label dstHostLabel = new Label("Enter (optional Host Filter)");
        dstHostLabel.getStyleClass().add("subtitle-label");
        TextField dstHostTextField = new TextField();
        dstHostTextField.setPromptText("2041:0000:140F::875B:131B");
        dstHostTextField.getStyleClass().add("textbox-label");
        dstHostTextField.setText("");
        uiButtonHandler.dstHostProperty().bind(dstHostTextField.textProperty());

        // TODO: add code for protocols as well. Maybe a drop down menu?


        // Create a horizontal line to separate the two sections
        Separator separator = new Separator();
        separator.getStyleClass().add("separator");

        // Create start and "stop & save" buttons in hbox
        HBox buttonBox = new HBox();
        buttonBox.getStyleClass().add("button-box");

        // Create a button to query data
        Button retrieveButton = new Button("Retrieve Data");
        uiButtonHandler.setRetrieveButton(retrieveButton);
        retrieveButton.getStyleClass().add("button");
        retrieveButton.setOnAction(uiButtonHandler);

        // Create a button to filter data
        Button filterButton = new Button("Update View");
        uiButtonHandler.setFilterButton(filterButton);
        filterButton.getStyleClass().add("button");
        filterButton.setOnAction(uiButtonHandler);

        // Add buttons to button box
        filterButton.setMaxWidth(Double.MAX_VALUE);
        retrieveButton.setMaxWidth(Double.MAX_VALUE);
        buttonBox.getChildren().addAll(filterButton, retrieveButton);
        HBox.setHgrow(filterButton, Priority.ALWAYS);
        HBox.setHgrow(retrieveButton, Priority.ALWAYS);

        // Add all the elements to the root pane
        controlPane.getChildren().addAll(titleLabel, startTimeLabel, startTimeTextField, endTimeLabel, endTimeTextField, observedHostLabel, observedHostTextField, dstHostLabel, dstHostTextField, separator, buttonBox);

    }

}
