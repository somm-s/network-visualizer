package ch.cydcampus.hickup.core;

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
    
    

}
