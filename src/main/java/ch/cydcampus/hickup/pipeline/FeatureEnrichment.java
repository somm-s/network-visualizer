package ch.cydcampus.hickup.pipeline;

/*
 * Interface for feature enrichment. Feature enrichment is the process of
 * adding new features to an abstraction. This is done by the feature
 * 
 */
public interface FeatureEnrichment {
    
    public void enrich(Abstraction abstraction);
    

}
