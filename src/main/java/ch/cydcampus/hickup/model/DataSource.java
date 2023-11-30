package ch.cydcampus.hickup.model;

/*
 * Supports multiple data sources to stream network data from.
 */
public interface DataSource {

    /*
     * Consumes a token from the data source. If data source cannot provide any more tokens, returns null.
     */
    public Token consume() throws InterruptedException;
    public void stopProducer();
    public void registerReader();
}
