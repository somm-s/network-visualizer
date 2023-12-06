package ch.cydcampus.hickup.model;

/*
 * Token tree in memory that is used to store tokens. Thread-safe.
 */
public class DataModel {    
    private Token root = new ParallelToken();
    private TokenPool tokenPool = TokenPool.getPool();

    public Token getRoot() {
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

    public synchronized String toString() {
        return root.deepToString();
    }

    public synchronized void clear() {
        root.deallocate();
        root = new ParallelToken();
    }
}
