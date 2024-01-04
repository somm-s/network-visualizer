package ch.cydcampus.hickup.core;

import java.io.IOException;
import java.util.List;

import ch.cydcampus.hickup.core.abstraction.Abstraction;
import ch.cydcampus.hickup.core.abstraction.AbstractionTree;
import ch.cydcampus.hickup.core.feature.Feature;
import ch.cydcampus.hickup.core.filter.Filter;
import ch.cydcampus.hickup.core.source.DataSource;

/*
 * Full pipeline. Contains the internal state of packets and abstractions as a tree.
 * Contains filter elements and feature enrichment elements for packets.
 * 
 * Initialization:
 * Before pipeline is functional, it needs to be initialized with a configuration file.
 * From the configuration, the Abstraction Tree is constructed (array of nodes).
 * The abstraction tree defines in which order the abstractions are applied and which states are shared among them.
 * Afterwards, the model is initialized with the abstraction tree.
 * The datasource, filter and feature enrichment module is initialized based on the configuration file.
 * 
 * Processing:
 * Each packet is consumed from the datasource and passed to the filter and enrichment modules and then added to the model.
 * 
 */
public class Pipeline {
    
    ConfigurationLoader configurationLoader;
    DataSource dataSource;
    List<Filter> filters;
    List<Feature> features;
    AbstractionTree abstractionTree;

    public Pipeline(String configurationFile) throws IOException {
        this.configurationLoader = new ConfigurationLoader(configurationFile);
    }

    public void initialize() {
        configurationLoader.loadConfiguration();
        this.dataSource = configurationLoader.getDataSource();
        this.dataSource.registerReader();
        this.filters = configurationLoader.getFilters();
        this.features = configurationLoader.getFeatures();
        this.abstractionTree = configurationLoader.getAbstractionTree();
    }

    /*
     * Processes the next packet from the data source.
     */
    public boolean process() throws InterruptedException {
        Packet packet = dataSource.consume();
        if(packet != null) {
            for(Filter filter : filters) {
                if(filter.filterMatch(packet)) {
                    return true;
                }
            }
            for(Feature feature : features) {
                feature.enrichFeature(packet);
            }
            abstractionTree.addPacket(packet);
            return true;
        } 
        return false;
    }

    /*
     * Stops the data source from producing more packets.
     */
    public void stop() throws InterruptedException {
        dataSource.stopProducer();
        // finish the elements in the consumer buffer
        // TODO: clean up. This is a hack to make sure all packets are processed.
        while(process());
    }

    public static void main(String[] args) {
        Pipeline pipeline = null;
        try {
            pipeline = new Pipeline("simple_config.json");
            pipeline.initialize();
            while(pipeline.process());
            pipeline.stop();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        Abstraction root = pipeline.abstractionTree.getRoot();
        StringBuilder sb = new StringBuilder();
        System.out.println(root.deepToString(sb).toString());
    }



    

}
