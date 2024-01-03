package ch.cydcampus.hickup.core.abstraction;

public class PacketNode implements Node {

    private int layer;
    private String name;
    private NodeType nodeType;

    public PacketNode(int layer, String name, String type) {
        this.layer = layer;
        this.name = name;
        this.nodeType = NodeType.fromString(type);
    }

    @Override
    public NodeType getNodeType() {
        return nodeType;
    }

    @Override
    public int[] getChildren() {
        return null;
    }

    @Override
    public String getLayerName() {
        return name;
    }

    @Override
    public int getLayer() {
        return layer;
    }

    @Override
    public TemporalRule getTemporalRule() {
        throw new RuntimeException("Packet layer does not have a temporal rule.");
    }

    @Override
    public SpatialRule getSpatialRule() {
        throw new RuntimeException("Packet layer does not have a spatial rule.");
    }
    
}
