package com.hickup;
import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.paint.*;
import javafx.stage.Stage;
import javafx.scene.canvas.*;

public class Drawboard extends Application {

    public static void main(String[] args) {
        launch(args);
    } 

    @Override
    public void start(Stage primaryStage) {
        Group root = new Group();
        Scene s = new Scene(root, 300, 300, Color.BLACK);
        primaryStage.setScene(s);

        final Canvas canvas = new Canvas(250,250);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        
        gc.setFill(Color.BLUE);
        gc.fillRect(75,75,100,100);

        root.getChildren().add(canvas);
        primaryStage.show();

    }

    

}
