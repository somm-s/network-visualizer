/*
 * Simple example of a progress bar that makes use of javafx.concurrent.Task.
 */

package com.sandbox;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.stage.Stage;

public class ProgressBar extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Progress Bar Example 1");

        // create a progress bar.
        javafx.scene.control.ProgressBar pBar = new javafx.scene.control.ProgressBar();

        // set initial value.
        pBar.setProgress(0.0);


        javafx.scene.control.Button startButton = new javafx.scene.control.Button("Start");
        javafx.scene.control.Button stopButton = new javafx.scene.control.Button("Stop");

        // create a task.
        javafx.concurrent.Task<Void> task = new javafx.concurrent.Task<Void>() {
            @Override
            protected Void call() throws Exception {
                for (int i = 0; i < 10; i++) {
                    if (isCancelled()) {
                        break;
                    }
                    Thread.sleep(2000);
                    System.out.println("Iteration " + i);
                    updateProgress(i + 1, 10);
                }
                return null;
            }
        };

        // bind progress property.
        pBar.progressProperty().bind(task.progressProperty());

        startButton.setOnAction((event) -> {
            new Thread(task).start();
        });

        stopButton.setOnAction((event) -> {
            task.cancel();
        });

        javafx.scene.layout.VBox root = new javafx.scene.layout.VBox();
        root.getChildren().addAll(pBar, startButton, stopButton);

        javafx.scene.Scene scene = new javafx.scene.Scene(root, 300, 250);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
}
