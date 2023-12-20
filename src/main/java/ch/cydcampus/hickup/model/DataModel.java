package ch.cydcampus.hickup.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

import ch.cydcampus.hickup.model.Token.TokenType;
import ch.cydcampus.hickup.util.MultipleReaderRingBuffer;

/*
 * Token tree in memory that is used to store tokens. Thread-safe.
 */
public class DataModel {    

    private static final int TOKENIZATION_LEVEL = Token.INTERACTION_LAYER;

    private Token root = new ParallelToken();
    private TokenPool tokenPool = TokenPool.getPool();
    private MultipleReaderRingBuffer ringBuffer = new MultipleReaderRingBuffer(10000);

    public synchronized Token getRoot() {
        return root;
    }

    /*
     * Inserts a token into the token tree.
     * 
     * @param token The token to insert.
     * @param combinationRule The combination rule to use.
     */
    public synchronized void insertToken(Token packetToken, CombinationRule combinationRule) {
        Token current = root;
        Token child = null;
        do {
            current.addSubToken(packetToken);
            child = current.getDecidingChild(packetToken);
            if(child == null || !combinationRule.belongsToToken(child, packetToken)) {
                if(child != null && child.getLevel() == TOKENIZATION_LEVEL) {
                    assert(current instanceof SequentialToken); // only chronologically ordered tokens can be used for tokenization
                    ringBuffer.produce(child);
                }

                child = current.createNewSubToken(packetToken, tokenPool);
            }
            current = child;
        } while(child != null && current.getLevel() < Token.BURST_LAYER);

        // Invariant: current is a BURST_LAYER token. Packets are always added, regardless of the combination rule.
        assert current.getLevel() == Token.BURST_LAYER;

        // add packet to current BURST_LAYER token
        current.addSubToken(packetToken);
        current.createNewSubToken(packetToken, tokenPool);
    }

    public Token consume() throws InterruptedException {
        return (Token) ringBuffer.consume();
    }

    public void registerReader() {
        ringBuffer.registerReader();
    }

    public synchronized void finishTokenStream() {
        Queue<Collection<Token>> bfsQueue = new LinkedList<>();
        bfsQueue.add(root.getSubTokens());

        while(!bfsQueue.isEmpty()) {
            Collection<Token> tokens = bfsQueue.remove();
            for(Token t : tokens) {

                if(t.getLevel() == TOKENIZATION_LEVEL) {
                    ringBuffer.produce(t);
                } else if(t.getLevel() < TOKENIZATION_LEVEL) {
                    // if parallel token take all in bfs
                    if(TokenType.fromLayer(t.getLevel()) == TokenType.PARALLEL) {
                        bfsQueue.add(t.getSubTokens());
                    } else {
                        // if sequential token only take deciding child
                        Token decidingChild = t.getDecidingChild(null);
                        if(decidingChild != null) {
                            bfsQueue.add(new LinkedList<Token>() {{ add(decidingChild); }});
                        }
                    }
                }
            }
        }

    }

    public synchronized String toString() {
        return root.deepToString();
    }

    public synchronized void clear() {
        root.deallocate();
        root = new ParallelToken();
    }
}
