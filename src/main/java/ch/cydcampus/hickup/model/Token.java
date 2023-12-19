package ch.cydcampus.hickup.model;

import java.util.Collection;
import ch.cydcampus.hickup.util.TimeInterval;

/*
 * Represents a token in the token tree.
 * This interface is used for all layers of abstraction.
 * Every token is either a ParallelToken or a SequentialToken:
 * - A ParallelToken is a token where children can be in parallel 
 *   and the Combination rule is not based on timings.
 * - A SequentialToken is a token where children are in sequence 
 *   and the Combination rule is based on timings of the last child.
 */
public interface Token {

    enum TokenType {
        PARALLEL, SEQUENTIAL;
        public static TokenType fromLayer(int Layer) {
            switch(Layer) {
                case ROOT_LAYER:
                    return SEQUENTIAL;
                case ACTIVITY_LAYER:
                    return PARALLEL;
                case DISCUSSION_LAYER:
                    return SEQUENTIAL;
                case INTERACTION_LAYER:
                    return PARALLEL;
                case FLOW_INTERACTION_LAYER:
                    return SEQUENTIAL;
                case OBJECT_BURST_LAYER:
                    return SEQUENTIAL;
                case BURST_LAYER:
                    return SEQUENTIAL;
                case PACKET_LAYER:
                    return SEQUENTIAL;
                default:
                    throw new IllegalArgumentException("Unknown layer: " + Layer);
            }
        }
    };

    // Numeric equivalents of each layer:
    int ROOT_LAYER = -1;
    int ACTIVITY_LAYER = 0;
    int DISCUSSION_LAYER = 1;
    int INTERACTION_LAYER = 2;
    int FLOW_INTERACTION_LAYER = 3;
    int OBJECT_BURST_LAYER = 4;
    int BURST_LAYER = 5;
    int PACKET_LAYER = 6;

    public TimeInterval getTimeInterval();
    public TokenState getState();
    public int getLevel();
    public void setLevel(int level);
    public Collection<Token> getSubTokens();

    /*
     * Adds the effect of a new packet belonging to this token, adjusting the time interval and its state.
     */
    public void addSubToken(Token packetToken);

    /*
     * Returns the child of this token that can be used together with the CombinationRule
     * to decide whether a new subToken should be created.
     */
    public Token getDecidingChild(Token packetToken);

    /*
     * Create new sub token based on the given packet token. Return created token.
     * Returns null and has no effect if on level of packets.
     */
    public Token createNewSubToken(Token packetToken, TokenPool tokenPool);


    /*
     * Deallocates node and all child nodes.
     */
     public void deallocate();

    /*
     * Returns a string representation of the token tree.
     */
    public String deepToString();

}
