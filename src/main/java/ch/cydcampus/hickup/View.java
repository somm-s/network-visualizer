package ch.cydcampus.hickup;

import java.sql.Time;

import ch.cydcampus.hickup.model.DataModel;
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

    public void setController(Controller controller, Stage primaryStage, DataModel model) {
        this.controller = controller;
        initializeUI(primaryStage, model);
    }

    private void initializeUI(Stage primaryStage, DataModel model) {
        // Initialize canvas
        canvas = new TimelineCanvas(model);
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
        filterTextField = new TextField();
        applyFilterButton = new Button("Play/Pause");

        // Set up event handlers
        applyFilterButton.setOnAction(event -> {
            canvas.togglePlayMode();
        });

        // handler for text field
        filterTextField.setOnAction(event -> {
            String filter = filterTextField.getText();
            canvas.setFilter(filter);
        });

        canvas.setOnScroll(event -> {
            canvas.handleScroll(event.getDeltaY());
        });
        canvas.setOnMousePressed(event -> {
            canvas.handleMousePressed(event.getX());
        });

        canvas.setOnMouseDragged(event -> {
            canvas.handleMouseDragged(event.getX());
        });

        controlPane.getChildren().addAll(filterTextField, applyFilterButton);
    }

    public void updateCanvas() {
        canvas.draw();
    }


}
