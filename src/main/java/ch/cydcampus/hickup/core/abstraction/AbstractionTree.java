package ch.cydcampus.hickup.core.abstraction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.cydcampus.hickup.core.Packet;
import ch.cydcampus.hickup.core.abstraction.Node.NodeType;

/*
 * Internal representation of all data.
 * Takes as input an array of Abstraction and Selection Nodes that is topologically sorted.
 * For an added Packet Token, it applies the rules in the given topological structure.
 */
public class AbstractionTree {
    
    private Node[] nodes;
    private Abstraction[] roots;

    public AbstractionTree(Node[] nodes) {
        this.nodes = nodes;
        this.roots = new Abstraction[nodes.length];

        // find roots --> nodes where no other node points to
        Set<Integer> rootIndices = new HashSet<>();
        for(int i = 0; i < nodes.length; i++) {
            rootIndices.add(i);
        }
        for(int i = 0; i < nodes.length; i++) {
            if(nodes[i].getChildren() != null) {
                for(int child : nodes[i].getChildren()) {
                    rootIndices.remove(child);
                }
            }
        }

        // initialize root nodes
        for(int rootIndex : rootIndices) {
            if(nodes[rootIndex].getNodeType() == NodeType.SpatialAbstractionNode) {
                roots[rootIndex] = new SpatialAbstraction(nodes[rootIndex].getLayer(), nodes[rootIndex].getChildren(), nodes[rootIndex].getSpatialRule());
            } else {
                roots[rootIndex] = new TemporalAbstraction(nodes[rootIndex].getLayer());
            }
        }
    }

    public void addPacket(Packet packet) {
        
        Abstraction[] current = new Abstraction[nodes.length];
        boolean[] created = new boolean[nodes.length];
        for(int i = 0; i < roots.length; i++) {
            if(roots[i] != null) {
                current[i] = roots[i];
            }
        }
        List<Abstraction> leafAbstractions = new ArrayList<>();

        for(int i = 0; i < nodes.length; i++) {
            if(nodes[i].getChildren()  == null) {
                leafAbstractions.add(current[i]);
                continue;
            }
            current[i].addToState(packet);

            for(int childLayer : nodes[i].getChildren()) {

                // case I: demultiplexing --> cached abstraction
                if(current[childLayer] != null && created[childLayer]) {
                    current[i].addChildAbstraction(current[childLayer], packet); // TODO: case that it is added to old one still exists...
                    continue;
                } else if(current[childLayer] != null) { // added automatically through other computation path
                    continue;
                }

                // case II: first time --> check for abstraction creation
                Abstraction child = current[i].getDecidingAbstraction(childLayer, packet);
                boolean createNewChild = false;
                if(child != null && nodes[i].getNodeType() == NodeType.TemporalAbstractionNode) {
                    createNewChild = !nodes[i].getTemporalRule().belongsTo(current[i].getLastPacket(childLayer), packet);
                } else if(child == null) {
                    createNewChild = true;
                }
                // create new abstraction
                if(createNewChild) {
                    if(nodes[childLayer].getNodeType() == NodeType.SpatialAbstractionNode) {
                        child = new SpatialAbstraction(childLayer, nodes[childLayer].getChildren(), nodes[childLayer].getSpatialRule());
                    } else {
                        child = new TemporalAbstraction(childLayer);
                    }
                    created[childLayer] = true;
                    current[i].addChildAbstraction(child, packet);
                }
                // cache abstraction for demultiplexing
                current[childLayer] = child;
            }

        }

        for(Abstraction leafAbstraction : leafAbstractions) {
            leafAbstraction.addToState(packet);
            leafAbstraction.addChildAbstraction(packet, packet);
        }

    }

    public Abstraction getRoot(int index) {
        return roots[index];
    }

    public Node[] getNodes() {
        return nodes;
    }

}
