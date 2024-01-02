package ch.cydcampus.hickup.view;

import java.io.IOException;

import ch.cydcampus.hickup.App;
import javafx.fxml.FXML;

public class SecondaryController {

    @FXML
    private void switchToPrimary() throws IOException {
        App.setRoot("primary");
    }


}