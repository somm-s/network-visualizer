package com.burstmeter;

import javafx.application.Application;
import javafx.stage.Stage;

public class MainApplication extends Application {
    
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {


        NetworkDataCapture ndc = new NetworkDataCapture();
        Thread t = new Thread(ndc);
        t.start();


        // Initialize your model, view, and controller
        View view = new View();
        Controller controller = new Controller(view, Model.getInstance());

        // Set up the UI components
        view.setController(controller, primaryStage);

        // Start the periodic update
        controller.startPeriodicUpdate();

        // Show the primary stage
        primaryStage.show();
    }
}
