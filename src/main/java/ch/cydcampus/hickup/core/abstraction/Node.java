package ch.cydcampus.hickup.core.abstraction;

/*
 * Building block of meta datastructure describing abstraction tree in memory.
 * Is implemented by SelectionNode and AbstractionNode.
 */
public interface Node {
    
    public static enum NodeType {
        TemporalAbstractionNode, SpatialAbstractionNode, PacketNode;
        public static NodeType fromString(String type) {
            if (type.equals("temporal")) {
                return TemporalAbstractionNode;
            } else if (type.equals("spatial")) {
                return SpatialAbstractionNode;
            } else if (type.equals("packet")) {
                return PacketNode;
            } else {
                throw new IllegalArgumentException("Unknown node type: " + type);
            }
        }
    }

    public NodeType getNodeType();
    public int[] getChildren();
    public String getLayerName();

    /*
     * Returns the layer of the node. Layers are counted from 0 and globally resemble a topologically sorted list of nodes.
     */
    public int getLayer();


    /*
     * Based on Node type, one of the following methods is called.
     */
    public TemporalRule getTemporalRule();
    public SpatialRule getSpatialRule();

}
