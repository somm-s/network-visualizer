package ch.cydcampus.hickup.model;

import java.util.Collection;
import java.util.List;
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
        PARALLEL, SEQUENTIAL
    };

    // Layers with respective types (parallel or sequential children):
    TokenType ROOT_LAYER_TYPE = TokenType.PARALLEL; // root.
    TokenType DISCUSSION_LAYER_TYPE = TokenType.SEQUENTIAL; // bidirectional host-to-host pairs
    TokenType INTERACTION_LAYER_TYPE = TokenType.PARALLEL; // interactions
    TokenType FLOW_INTERACTION_LAYER_TYPE = TokenType.SEQUENTIAL; // flow interactions
    TokenType OBJECT_BURST_LAYER_TYPE = TokenType.SEQUENTIAL; // object bursts
    TokenType BURST_LAYER_TYPE = TokenType.PARALLEL; // bursts
    TokenType PACKET_LAYER_TYPE = TokenType.SEQUENTIAL; // packets

    // Numeric equivalents of each layer:
    int ROOT_LAYER = 0;
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
     * to decide whether a new new subToken should be created.
     */
    public Token getDecidingChild(Token packetToken);

    /*
     * Create new sub token based on the given packet token. Return created token.
     * Returns null and has no effect if on level of packets.
     */
    public Token createNewSubToken(Token packetToken, TokenPool tokenPool);

    /*
     * Returns a string representation of the token tree.
     */
    public String deepToString();
}
