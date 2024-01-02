package ch.cydcampus.hickup.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/*
 * Configuration class. Contains the configuration of a pipeline.
 */

public class ConfigurationLoader {

    public static enum DataSourceType {
        CSV_FOLDER, CSV, PCAP_FOLDER, PCAP, SQL, INTERFACE, DOUBLE_INTERFACE;
        public static DataSourceType fromString(String type) {
            switch(type) {
                case "csv_folder":
                    return CSV_FOLDER;
                case "csv":
                    return CSV;
                case "pcap_folder":
                    return PCAP_FOLDER;
                case "pcap":
                    return PCAP;
                case "sql":
                    return SQL;
                case "interface":
                    return INTERFACE;
                case "double_interface":
                    return DOUBLE_INTERFACE;
                default:
                    return null;
            }
        }
    }

    private JsonNode configuration;
    private List<JsonNode> filterConfigs;
    private List<JsonNode> enrichmentConfigs;
    private JsonNode modelConfig;
    private JsonNode dataSourceConfig;
    private ObjectMapper objectMapper;

    /* Loads json file and initializes all fields */
    public ConfigurationLoader(String configFilePath) throws IOException {
        this.objectMapper = new ObjectMapper();
        this.configuration = objectMapper.readTree(new File(configFilePath));
        this.filterConfigs = new LinkedList<>();
        this.enrichmentConfigs = new LinkedList<>();

        int numElements = configuration.size();
        for(int i = 0; i < numElements; i++) {
            JsonNode element = configuration.get(i);
            String type = element.get("blocktype").asText();
            switch(type) {
                case "datasource":
                    dataSourceConfig = element;
                    break;
                case "filter":
                    filterConfigs.add(element);
                    break;
                case "feature":
                    enrichmentConfigs.add(element);
                    break;
                case "abstractiontree":
                    modelConfig = element;
                    break;
                default:
                    System.err.println("Unknown element type: " + type);
            }
        }
    }

    private void createFilters(JsonNode filterConfig) {
        String filterType = filterConfig.get("type").asText();
        switch(filterType) {
            case "attribute":
                break;
            case "ip":
                break;
            case "host_pair":
                break;
            case "protocol":
                break;
            case "port":
                break;
            case "port_pair":
                break;
            case "time":
                break;
            case "packet_size":
                break;
            default:
                System.err.println("Unknown filter type: " + filterType);
        }
    }

    public DataSource getDataSource() {

        String dataSourceType = dataSourceConfig.get("type").asText();
        DataSource dataSource = null;

        switch(dataSourceType) {
        }

        return null;
    }

    public List<Filter> getFilters() {
        return null;

    }

    public List<FeatureEnrichment> getFeatureEnrichments() {
        return null;

    }

    public AbstractionTree getAbstractionTree() {
        return null;

    }


    public static void main(String[] args) throws IOException {
        ConfigurationLoader config = new ConfigurationLoader("config.json");
        System.out.println("Done.");
    }

    
}
