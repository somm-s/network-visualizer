package ch.cydcampus.hickup;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapNativeException;

import ch.cydcampus.hickup.model.AbstractionModule;
import ch.cydcampus.hickup.model.CombinationRule;
import ch.cydcampus.hickup.model.DataBaseSource;
import ch.cydcampus.hickup.model.DataIterator;
import ch.cydcampus.hickup.model.DataModel;
import ch.cydcampus.hickup.model.DataSource;
import ch.cydcampus.hickup.model.DoubleInterfaceSource;
import ch.cydcampus.hickup.model.FileSource;
import ch.cydcampus.hickup.model.NetworkCaptureSource;
import ch.cydcampus.hickup.model.SimpleCombinationRule;
import ch.cydcampus.hickup.model.Token;
import ch.cydcampus.hickup.util.Callback;
import ch.cydcampus.hickup.util.LayerStatistics;
import ch.cydcampus.hickup.view.View;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApplication extends Application {
    
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws PcapNativeException, NotOpenException, IOException, InterruptedException {

        // loading screen
        DataModel dataModel = new DataModel();
        // while(true) {
        //     Tokenizer tokenizer = new Tokenizer(dataModel);
        //     tokenizer.start();

        //     DataSource hostToHostSource = new DataBaseSource("localhost", 5432, "ls22", "lab", "lab", "packets", "", "", "", "");
        //     CombinationRule combinationRule = new SimpleCombinationRule();
        //     AbstractionModule abstractionModule = new AbstractionModule(dataModel, hostToHostSource, combinationRule);
        //     abstractionModule.start();
        //     abstractionModule.join();
        //     dataModel.finishTokenStream();
        //     tokenizer.stopThread();
        // }

        // Initialize your model, view, and controller
        View view = new View(primaryStage);
        Controller controller = new Controller(view, dataModel);
        controller.startApplication();


        // create data structure
        // DataSource hostToHostSource = new DataBaseSource("localhost", 5432, "ls22", "lab", "lab", "packets", "", "", "", "");
        // CombinationRule combinationRule = new SimpleCombinationRule();
        // AbstractionModule abstractionModule = new AbstractionModule(dataModel, hostToHostSource, combinationRule);
        // tokenizer.start();
        // abstractionModule.start();
        // abstractionModule.join();
        // wait for tokenizer to empty queue (5s)


        // // collect sizes of object burst layer
        // Callback callback = new LayerStatistics();
        // DataIterator dataIterator = new DataIterator(dataModel, callback, 3);
        // dataIterator.iterate();
        // List<Double> statistics = (List<Double>) callback.getResult();

        // // convert to double array
        // double[] data = new double[statistics.size()];
        // for(int i = 0; i < statistics.size(); i++) {
        //     data[i] = Math.log(statistics.get(i));
        // }

        // // scatter plot
        // view.createSimpleHistogram(data, 200);
    }
}
