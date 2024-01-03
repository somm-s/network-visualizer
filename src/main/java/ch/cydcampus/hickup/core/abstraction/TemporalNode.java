package ch.cydcampus.hickup.core.abstraction;

public class TemporalNode implements Node {

    int layer;
    String layerName;
    NodeType nodeType;
    TemporalRule temporalRule;
    int[] children;

    public TemporalNode(int layer, String layerName, String type, TemporalRule temporalRule, int[] children) {
        this.layer = layer;
        this.layerName = layerName;
        this.nodeType = NodeType.fromString(type);
        this.temporalRule = temporalRule;
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
        return temporalRule;
    }

    @Override
    public SpatialRule getSpatialRule() {
        throw new UnsupportedOperationException("Not supported for TemporalNode");
    }

    @Override
    public String getLayerName() {
        return layerName;
    }
    
}
