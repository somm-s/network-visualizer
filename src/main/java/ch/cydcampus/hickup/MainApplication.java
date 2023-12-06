package ch.cydcampus.hickup;

import java.io.IOException;

import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapNativeException;

import ch.cydcampus.hickup.model.AbstractionModule;
import ch.cydcampus.hickup.model.CombinationRule;
import ch.cydcampus.hickup.model.DataModel;
import ch.cydcampus.hickup.model.DataSource;
import ch.cydcampus.hickup.model.DoubleInterfaceSource;
import ch.cydcampus.hickup.model.FileSource;
import ch.cydcampus.hickup.model.NetworkCaptureSource;
import ch.cydcampus.hickup.model.SimpleCombinationRule;
import ch.cydcampus.hickup.view.View;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApplication extends Application {
    
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws PcapNativeException, NotOpenException, IOException {

        // loading screen

        DataModel dataModel = new DataModel();

        // Initialize your model, view, and controller
        View view = new View(primaryStage);
        Controller controller = new Controller(view, dataModel);

        controller.startApplication();
    }
}
