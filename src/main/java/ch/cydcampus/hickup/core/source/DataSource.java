package ch.cydcampus.hickup.core.source;

import java.util.Set;

import ch.cydcampus.hickup.core.Packet;
import ch.cydcampus.hickup.core.filter.Filter;
import ch.cydcampus.hickup.core.filter.Filter.FilterType;

/*
 * Supports multiple data sources to stream network data from.
 */
public interface DataSource {

    /*
     * Consumes a token from the data source. If data source cannot provide any more tokens, returns null.
     */
    public Packet consume() throws InterruptedException;

    /*
     * Stops the data source immediately from producing more tokens.
     * There might still be packets buffered which can be consumed.
     */
    public void stopProducer();

    /*
     * Registers a reader to the data source.
     */
    public void registerReader();

    public Set<FilterType> getSupportedFilters();

    public void setFilter(Filter filter);
}
