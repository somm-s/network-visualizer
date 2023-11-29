package ch.cydcampus.hickup;

import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapNativeException;

import ch.cydcampus.hickup.model.AbstractionModule;
import ch.cydcampus.hickup.model.CombinationRule;
import ch.cydcampus.hickup.model.DataModel;
import ch.cydcampus.hickup.model.DataSource;
import ch.cydcampus.hickup.model.NetworkCaptureSource;
import ch.cydcampus.hickup.model.ParallelToken;
import ch.cydcampus.hickup.model.SimpleCombinationRule;
import ch.cydcampus.hickup.model.Token;

public class Runner {

    public static void main(String[] args) throws PcapNativeException, NotOpenException {

        DataSource dataSource = new NetworkCaptureSource("wlp0s20f3");
        DataModel dataModel = new DataModel();
        CombinationRule combinationRule = new SimpleCombinationRule();
        AbstractionModule abstractionModule = new AbstractionModule(dataModel, dataSource, combinationRule);
        abstractionModule.start();

        // wait 10 seconds
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("TEST");
        System.out.println(dataModel.toString());

        // stop the threads
        abstractionModule.stopThread();
        dataSource.stopProducer();

    }
    
}
