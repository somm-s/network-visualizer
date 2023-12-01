package ch.cydcampus.hickup.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

import ch.cydcampus.hickup.util.TimeInterval;

/*
 * Token where the children are in sequence and the 
 * Combination rule is based on timings of the last child.
 */
public class SequentialToken implements Token {
    
    private static int counter = 0;
    private ConcurrentLinkedDeque<Token> subTokens;
    private TokenState state;
    private TimeInterval timeInterval;
    private int level;
    private int id;

    /*
     * Creates a new token.
     * 
     * @param parent The parent token.
     * @param subTokens The sub tokens. Pass null if this token has no sub tokens.
     * @param state The data associated with the token.
     * @param timeInterval The time interval that the token and all subtokens span.
     * @param level The level of the token in the token tree. Root token has level 0.
     */
    public SequentialToken(TokenState state, TimeInterval timeInterval, int level) {
        this.state = state;
        this.timeInterval = timeInterval;
        this.subTokens = new ConcurrentLinkedDeque<Token>();
        this.level = level;
        this.id = counter++;
    }

    public TokenState getState() {
        return state;
    }

    public TimeInterval getTimeInterval() {
        return timeInterval;
    }

    public synchronized Collection<Token> getSubTokens() {
        return subTokens;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;        
    }

    @Override
    public void addSubToken(Token packetToken) {
        if(timeInterval == null) {
            timeInterval = packetToken.getTimeInterval();
        } else {
            timeInterval = timeInterval.union(packetToken.getTimeInterval());
        }

        state.addBytesFromOther(packetToken.getState());
    }

    @Override
    public Token getDecidingChild(Token packetToken) {
        if(subTokens.isEmpty()) {
            return null;
        }
        return subTokens.getLast();
    }

    @Override
    public synchronized Token createNewSubToken(Token packetToken, TokenPool tokenPool) {
        Token newSubToken = null;
        if(level == DISCUSSION_LAYER) {
            newSubToken = tokenPool.allocateParallelToken(packetToken, level + 1);
            subTokens.add(newSubToken);
        } else if(level == FLOW_INTERACTION_LAYER || 
            level == OBJECT_BURST_LAYER) {
            newSubToken = tokenPool.allocateSequentialToken(packetToken, level + 1);
            subTokens.add(newSubToken);
        } else if(level == BURST_LAYER) {
            subTokens.add(packetToken);
        } else {
            throw new IllegalStateException(
                "Error: SequentialToken.createNewSubToken() called on level " + level + ".");
        }

        this.getState().incrementSubTokenCount();
        return newSubToken;
    }

    public String toString() {
        return "ST: " + id + " lvl: " + level + " ++" + subTokens.size() + "++ " + timeInterval;
    }

    @Override
    public String deepToString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.toString() + " " + state.toString());
        sb.append("\n");
        for(Token subToken : subTokens) {
            for(int i = 0; i < level; i++) {
                sb.append("  ");
            }
            sb.append(subToken.deepToString());
        }
        return sb.toString();
    }

}