package ch.cydcampus.hickup.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.cydcampus.hickup.core.abstraction.AbstractionTree;
import ch.cydcampus.hickup.core.abstraction.Node;
import ch.cydcampus.hickup.core.abstraction.PacketNode;
import ch.cydcampus.hickup.core.abstraction.SpatialNode;
import ch.cydcampus.hickup.core.abstraction.SpatialRule;
import ch.cydcampus.hickup.core.abstraction.TemporalNode;
import ch.cydcampus.hickup.core.abstraction.TemporalRule;
import ch.cydcampus.hickup.core.feature.Feature;
import ch.cydcampus.hickup.core.feature.FlowIdentifierFeature;
import ch.cydcampus.hickup.core.feature.HostPairIdentifierFeature;
import ch.cydcampus.hickup.core.filter.Filter;
import ch.cydcampus.hickup.core.filter.IPFilter;
import ch.cydcampus.hickup.core.filter.PacketSizeFilter;
import ch.cydcampus.hickup.core.filter.TimeFilter;
import ch.cydcampus.hickup.core.filter.Filter.FilterType;
import ch.cydcampus.hickup.core.source.DataBaseSource;
import ch.cydcampus.hickup.core.source.DataSource;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/*
 * Configuration class. Contains the configuration of a pipeline.
 */

public class ConfigurationLoader {

    private List<Filter> filters;
    private List<Feature> features;
    private DataSource dataSource;
    private AbstractionTree abstractionTree;
    
    private JsonNode configurationFileRoot;
    private JsonNode dataSourceConfig;
    private JsonNode abstractionTreeConfig;
    private ObjectMapper objectMapper;

    /* Loads json file and initializes all fields */
    public ConfigurationLoader(String configFilePath) throws IOException {
        this.objectMapper = new ObjectMapper();
        this.configurationFileRoot = objectMapper.readTree(new File(configFilePath));
        this.filters = new LinkedList<>();
        this.features = new LinkedList<>();

        int numElements = configurationFileRoot.size();
        for(int i = 0; i < numElements; i++) {
            JsonNode element = configurationFileRoot.get(i);
            String type = element.get("blocktype").asText();
            switch(type) {
                case "datasource":
                    if(dataSourceConfig != null) {
                        System.err.println("Multiple data sources defined. Only the last one will be used.");
                    }
                    dataSourceConfig = element;
                    break;
                case "filter":
                    filters.add(createFilter(element));
                    break;
                case "feature":
                    features.add(createFeature(element));
                    break;
                case "abstractiontree":
                    if(abstractionTreeConfig != null) {
                        System.err.println("Multiple abstraction trees defined. Only the last one will be used.");
                    }
                    abstractionTreeConfig = element;
                    break;
                default:
                    System.err.println("Unknown element type: " + type);
            }
        }
    }

    private Feature createFeature(JsonNode featureConfig) {
        String featureName = featureConfig.get("name").asText();

        Feature feature = null;
        switch(featureName) {
            case "host_pair_identifier":
                feature = new HostPairIdentifierFeature();
                break;
            case "flow_identifier":
                feature = new FlowIdentifierFeature();
                break;
            default:
                System.err.println("Unknown feature name: " + featureName);
                throw new UnsupportedOperationException("Feature creation failed.");
        }

        return feature;
    }

    private Filter createFilter(JsonNode filterConfig) {
        String filterType = filterConfig.get("type").asText();
        switch(filterType) {
            case "attribute":
                throw new UnsupportedOperationException("Attribute filter not implemented yet.");
            case "ip":
                List<String> ips = new LinkedList<>();
                for(JsonNode ip : filterConfig.get("values")) {
                    ips.add(ip.asText());
                }
                return new IPFilter(ips, filterConfig.get("policy").asText());
            case "host_pair":
                throw new UnsupportedOperationException("Host pair filter not implemented yet.");
            case "protocol":
                throw new UnsupportedOperationException("Protocol filter not implemented yet.");
            case "port":
                throw new UnsupportedOperationException("Port filter not implemented yet.");
            case "port_pair":
                throw new UnsupportedOperationException("Port pair filter not implemented yet.");
            case "time":
                return new TimeFilter(filterConfig.get("min").asText(), filterConfig.get("max").asText(), filterConfig.get("policy").asText());
            case "packet_size":
                return new PacketSizeFilter(filterConfig.get("min").asText(), filterConfig.get("max").asText(), filterConfig.get("policy").asText());
            default:
                System.err.println("Unknown filter type: " + filterType);
        }
        throw new UnsupportedOperationException("Filter creation failed.");
    }

    private void createDataSource() {

        String dataSourceType = dataSourceConfig.get("sourcetype").asText();
        switch(dataSourceType) {
            case "csv_folder":
                throw new UnsupportedOperationException("CSV folder data source not implemented yet.");
            case "csv":
                throw new UnsupportedOperationException("CSV data source not implemented yet.");
            case "pcap_folder":
                throw new UnsupportedOperationException("PCAP folder data source not implemented yet.");
            case "pcap":
                throw new UnsupportedOperationException("PCAP data source not implemented yet.");
            case "sql":
                String host = dataSourceConfig.get("host").asText();
                int port = dataSourceConfig.get("port").asInt();
                String database = dataSourceConfig.get("database").asText();
                String user = dataSourceConfig.get("user").asText();
                String password = dataSourceConfig.get("password").asText();
                String table = dataSourceConfig.get("table").asText();
                dataSource = new DataBaseSource(host, port, database, user, password, table);
                break;
            case "interface":
                throw new UnsupportedOperationException("Interface data source not implemented yet.");
            case "double_interface":
                throw new UnsupportedOperationException("Double interface data source not implemented yet.");
            default:
                System.err.println("Unknown data source type: " + dataSourceType);
                throw new UnsupportedOperationException("Data source creation failed.");
        }
    }

    private TemporalRule createTemporalRule(JsonNode temporalRuleConfig) {
        return new TemporalRule(temporalRuleConfig.get("timeout").asLong(), temporalRuleConfig.get("bidirectional").asBoolean());
    }

    private SpatialRule createSpatialRule(JsonNode spatialRuleConfig) {
        return new SpatialRule(spatialRuleConfig.get("attribute").asText());
    }

    private Node createNode(JsonNode nodeConfig) {
        String type = nodeConfig.get("type").asText();
        String name = nodeConfig.get("name").asText();
        List<Integer> children = new LinkedList<>();
        for(JsonNode child : nodeConfig.get("children")) {
            children.add(child.asInt());
        }
        int[] childrenArray = new int[children.size()];
        for(int i = 0; i < children.size(); i++) {
            childrenArray[i] = children.get(i);
        }

        switch(type) {
            case "temporal":
                TemporalRule temporalRule = createTemporalRule(nodeConfig.get("rule"));
                return new TemporalNode(nodeConfig.get("layer").asInt(), name, type, temporalRule, childrenArray);
            case "spatial":
                SpatialRule spatialRule = createSpatialRule(nodeConfig.get("rule"));
                return new SpatialNode(nodeConfig.get("layer").asInt(), name, type, spatialRule, childrenArray);
            case "packet":
                return new PacketNode(nodeConfig.get("layer").asInt(), name, type);
            default:
                System.err.println("Unknown node type: " + type);
                throw new UnsupportedOperationException("Node creation failed.");
        }
    }

    private void createAbstractionTree() {

        JsonNode nodes = abstractionTreeConfig.get("nodes");
        List<Node> abstractionTreeNodes = new LinkedList<>();
        for(JsonNode node : nodes) {
            abstractionTreeNodes.add(createNode(node));
        }

        Node[] abstractionTreeNodesArray = new Node[abstractionTreeNodes.size()];
        abstractionTreeNodesArray = abstractionTreeNodes.toArray(abstractionTreeNodesArray);
        java.util.Arrays.sort(abstractionTreeNodesArray, (a, b) -> a.getLayer() - b.getLayer());

        abstractionTree = new AbstractionTree(abstractionTreeNodesArray);

    }

    public void loadConfiguration() {
        createDataSource();
        Set<FilterType> supportedFilters = dataSource.getSupportedFilters();
        Iterator<Filter> filterIterator = filters.iterator();
        while(filterIterator.hasNext()) {
            Filter filter = filterIterator.next();
            if(supportedFilters.contains(filter.getFilterType())) {
                dataSource.setFilter(filter);
                filterIterator.remove();
            }
        }

        String[] attributeNames = new String[features.size()];
        HashMap<String, Integer> attributeIndices = new HashMap<>();


        // register features at static packet class
        int i = 0;
        for(Feature feature : features) {
            attributeNames[i] = feature.getFeatureName();
            attributeIndices.put(feature.getFeatureName(), i);
            i++;
        }
        Packet.registerFeatures(attributeNames, attributeIndices);
        createAbstractionTree();
    }


    public DataSource getDataSource() {
        return dataSource;

    }

    public List<Filter> getFilters() {
        return filters;

    }

    public List<Feature> getFeatures() {
        return features;
    }

    public AbstractionTree getAbstractionTree() {
        return abstractionTree;

    }


    public static void main(String[] args) throws IOException {
        ConfigurationLoader config = new ConfigurationLoader("config.json");
        config.loadConfiguration();
        System.out.println("Done.");
    }

    
}
