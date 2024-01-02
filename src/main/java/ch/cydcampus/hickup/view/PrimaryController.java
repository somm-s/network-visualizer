package ch.cydcampus.hickup.view;

import javafx.scene.canvas.Canvas;
import java.io.IOException;

import ch.cydcampus.hickup.App;
import javafx.fxml.FXML;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class PrimaryController {

    @FXML
    private Canvas canvas;

    @FXML
    private void switchToSecondary() throws IOException {
        App.setRoot("secondary");
    }


    public void initialize() {
        // You can perform initialization here if needed
        drawOnCanvas();
    }

    private void drawOnCanvas() {
        // Access the GraphicsContext of the Canvas
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Perform drawing operations
        gc.setFill(Color.BLUE);
        gc.fillRect(50, 50, 100, 100);
    }

}
