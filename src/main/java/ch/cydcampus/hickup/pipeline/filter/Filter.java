package ch.cydcampus.hickup.pipeline.filter;

import ch.cydcampus.hickup.core.Packet;

/* 
 * A filter is a description of a set of packets to be matched. A filter can be whitelist or blacklist policy.
 */
public interface Filter {
    
    /*
     * All available filter types. Each data source can support different types of filters. Filters are pushed to the data source if supported.
     */
    public static enum FilterType {
        ATTRIBUTE, IP, HOST_PAIR, PROTOCOL, PORT, PORT_PAIR, TIME, PACKET_SIZE, PREFIX, IP_VERSION;
        public static FilterType fromString(String type) {
            switch(type) {
                case "attribute":
                    return ATTRIBUTE;
                case "ip":
                    return IP;
                case "host_pair":
                    return HOST_PAIR;
                case "protocol":
                    return PROTOCOL;
                case "port":
                    return PORT;
                case "port_pair":
                    return PORT_PAIR;
                case "time":
                    return TIME;
                case "packet_size":
                    return PACKET_SIZE;
                case "prefix":
                    return PREFIX;
                case "ip_version":
                    return IP_VERSION;
                default:
                    return null;
            }
        }
    }

    /*
     * Unique identifier of the filter.
     */
    public String getFilterName();

    /*
     * Returns true if the filter matches the given object, i.e., the object is filtered out.
     */
    public boolean filterMatch(Packet packet);

    public FilterType getFilterType();

}