package ch.cydcampus.hickup.core.abstraction;

import ch.cydcampus.hickup.core.Packet;
import ch.cydcampus.hickup.core.abstraction.Node.NodeType;

/*
 * Internal representation of all data.
 * Takes as input an array of Abstraction and Selection Nodes that is topologically sorted.
 * For an added Packet Token, it applies the rules in the given topological structure.
 */
public class AbstractionTree {
    
    private Node[] nodes;
    private Abstraction root;

    public AbstractionTree(Node[] nodes) {
        this.nodes = nodes;

        if(nodes[0].getNodeType() == NodeType.SpatialAbstractionNode) {
            root = new SpatialAbstraction(nodes[0].getLayer(), nodes[0].getChildren(), nodes[0].getSpatialRule());
        } else {
            root = new TemporalAbstraction(nodes[0].getLayer());
        }
    }

    public void addPacket(Packet packet) {
        
        Abstraction[] current = new Abstraction[nodes.length];
        current[0] = root;
        System.out.println("Adding packet: " + packet);
        for(int i = 0; i < nodes.length; i++) {
            System.out.println("\n\nIteration " + i + " with node " + nodes[i].getLayer() + " and type " + nodes[i].getNodeType());
            if(nodes[i].getNodeType() == NodeType.PacketNode) {
                continue;
            }

            System.out.println("Adding packet to state " + current[i]);
            current[i].addToState(packet);
            System.out.println("Added to state " + current[i]);

            for(int childLayer : nodes[i].getChildren()) {
                System.out.println("Child layer: " + childLayer);
                Abstraction child = current[i].getDecidingAbstraction(childLayer, packet);
                System.out.println("Deciding abstraction: " + child);
                
                boolean createNewChild = false;
                if(child != null && nodes[i].getNodeType() == NodeType.TemporalAbstractionNode) {
                    createNewChild = !nodes[i].getTemporalRule().belongsTo(current[i].getLastPacket(childLayer), packet);
                } else if(child == null) {
                    createNewChild = true;
                }
                System.out.println("Create new child: " + createNewChild);

                if(createNewChild) {

                    if(current[childLayer] == null) { // layer has not been computed before --> no caching
                        if(nodes[childLayer].getNodeType() == NodeType.SpatialAbstractionNode) {
                            child = new SpatialAbstraction(childLayer, nodes[childLayer].getChildren(), nodes[childLayer].getSpatialRule());
                        } else if(nodes[childLayer].getNodeType() == NodeType.TemporalAbstractionNode) {
                            child = new TemporalAbstraction(childLayer);
                        } else { // Child is a packet node
                            child = packet;
                        }
                    } else {
                        child = current[childLayer];
                    }

                    current[i].addChildAbstraction(child, packet);
                }else if(nodes[childLayer].getNodeType() == NodeType.PacketNode) {
                    current[i].addChildAbstraction(child, packet);
                }

                current[childLayer] = child;
            }

        }

    }

    public Abstraction getRoot() {
        return root;
    }

    public Node[] getNodes() {
        return nodes;
    }

}
