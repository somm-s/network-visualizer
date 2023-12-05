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

        // DataSource dataSource = new NetworkCaptureSource("wlp0s20f3");
        // DataSource dataSource = new FileSource("/home/lab/Documents/networking/ls22/0/0/2022-04-18 20:43.csv");
        DataSource dataSource = new DoubleInterfaceSource("enx00e04c680105", "enx1c1adff824f9");
        DataModel dataModel = new DataModel();
        CombinationRule combinationRule = new SimpleCombinationRule();
        AbstractionModule abstractionModule = new AbstractionModule(dataModel, dataSource, combinationRule);
        abstractionModule.start();

        // Initialize your model, view, and controller
        View view = new View();
        Controller controller = new Controller(view);

        // Set up the UI components
        view.setController(controller, primaryStage, dataModel);

        // Start the periodic update
        controller.startPeriodicUpdate();

        // Show the primary stage
        primaryStage.show();

        // wait for keyboard input
        // System.in.read();
        // System.out.println(dataModel.toString());

        // stop the threads
        // abstractionModule.stopThread();
        // dataSource.stopProducer();
    }
}
