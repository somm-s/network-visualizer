package ch.cydcampus.hickup.model;

/*
 * Supports multiple data sources to stream network data from.
 */
public interface DataSource {
    public Token consume() throws InterruptedException;
    public void stopProducer();
    public void registerReader();
}
