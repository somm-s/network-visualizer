package ch.cydcampus.hickup.core.abstraction;

public class SpatialNode implements Node {

    int layer;
    String layerName;
    NodeType nodeType;
    SpatialRule spatialRule;
    int[] children;

    public SpatialNode(int layer, String layerName, String type, SpatialRule spatialRule, int[] children) {
        this.layer = layer;
        this.layerName = layerName;
        this.nodeType = NodeType.fromString(type);
        this.spatialRule = spatialRule;
        this.children = children;
    }

    @Override
    public NodeType getNodeType() {
        return nodeType;
    }

    @Override
    public int[] getChildren() {
        return children;
    }

    @Override
    public int getLayer() {
        return layer;
    }

    @Override
    public TemporalRule getTemporalRule() {
        throw new UnsupportedOperationException("Not supported for SpatialNode");
    }

    @Override
    public SpatialRule getSpatialRule() {
        return spatialRule;
    }

    @Override
    public String getLayerName() {
        return layerName;
    }
    
}
