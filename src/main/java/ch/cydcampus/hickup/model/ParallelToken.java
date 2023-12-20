package ch.cydcampus.hickup.model;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import ch.cydcampus.hickup.util.TimeInterval;

public class ParallelToken implements Token{

    private static int counter = 0;
    private ConcurrentHashMap<String, Token> subTokens;
    private TokenState state;
    private volatile TimeInterval timeInterval;
    private volatile int level;
    private int id;

    /*
     * Creates a new root token. Level defaults to 0.
     */
    public ParallelToken() {
        this.subTokens = new ConcurrentHashMap<String, Token>();
        this.state = new TokenState(0, 0, "", "", 0, 0, TokenState.Protocol.ANY);
        this.timeInterval = new TimeInterval();
        this.level = 0;
        this.id = counter++;
    }

    /*
     * Creates a new token.
     * 
     * @param parent The parent token.
     * @param subTokens The sub tokens. Pass null if this token has no sub tokens.
     * @param state The data associated with the token.
     * @param timeInterval The time interval that the token and all subtokens span.
     * @param level The level of the token in the token tree. Root token has level 0.
     */
    public ParallelToken(TokenState state, TimeInterval timeInterval, int level) {
        this.state = state;
        this.timeInterval = timeInterval;
        this.subTokens = new ConcurrentHashMap<String, Token>();
        this.level = level;
        this.id = counter++;
    }

    @Override
    public TimeInterval getTimeInterval() {
        return timeInterval;
    }

    @Override
    public TokenState getState() {
        return state;
    }

    @Override
    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public Collection<Token> getSubTokens() {
        return subTokens.values();
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
        Token decidingChild = null;
        if(this.level == ROOT_LAYER) {
            // get host pair
            String hostToHostIdentifier = packetToken.getState().getHostToHostIdentifier();
            decidingChild = subTokens.get(hostToHostIdentifier);
        } else if(this.level == INTERACTION_LAYER) {
            // get bidirectional flow identifier
            String bidirectionalFlowIdentifier = packetToken.getState().getBidirectionalFlowIdentifier();
            decidingChild = subTokens.get(bidirectionalFlowIdentifier);
        } else {
            throw new IllegalStateException("Error: ParallelToken.getDecidingChild() called on level " + this.level + ".");
        }
        return decidingChild;
    }

    @Override
    public Token createNewSubToken(Token packetToken, TokenPool tokenPool) {
        Token newSubToken = null;
        if(this.level == ROOT_LAYER) {
            String hostToHostIdentifier = packetToken.getState().getHostToHostIdentifier();
            newSubToken = tokenPool.allocateSequentialToken(packetToken, this.level + 1);
            subTokens.put(hostToHostIdentifier, newSubToken);
        } else if(this.level == INTERACTION_LAYER) {
            // create bidirectional flow identifier
            String bidirectionalFlowIdentifier = packetToken.getState().getBidirectionalFlowIdentifier();
            newSubToken = tokenPool.allocateSequentialToken(packetToken, this.level + 1);
            subTokens.put(bidirectionalFlowIdentifier, newSubToken);
        } else {
            throw new IllegalStateException("Error: ParallelToken.createNewSubToken() called on level " + this.level + ".");
        }

        this.getState().incrementSubTokenCount();
        return newSubToken;
    }

    public String toString() {
        return "PT: " + id + " lvl: " + level + " ++" + subTokens.size() + "++ " + timeInterval;
    }
    
    @Override
    public String deepToString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.toString() + " ");
        if(state == null) {
            sb.append("ROOT");
        } else {
            sb.append(state.toString());
        }
        sb.append("\n");
        for(Token subToken : subTokens.values()) {
            for(int i = 0; i < level; i++) {
                sb.append("  ");
            }
            sb.append(subToken.deepToString());
        }
        return sb.toString();
    }

    @Override
    public void deallocate() {
        for(Token subToken : subTokens.values()) {
            subToken.deallocate();
        }
        subTokens.clear();
        if(level != ROOT_LAYER) {
            TokenPool.getPool().releaseParallelToken(this);
        }
    }
}
