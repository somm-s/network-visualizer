package ch.cydcampus.hickup.model;

/*
 * Token tree in memory that is used to store tokens. Thread-safe.
 */
public class DataModel {    
    private Token root = new ParallelToken();
    private TokenPool tokenPool = TokenPool.getPool();

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
            Token decidingChild = current.getDecidingChild(packetToken);
            if(decidingChild == null) {
            } else {
            }

            if(decidingChild != null && combinationRule.belongsToToken(decidingChild, packetToken)) {
                child = decidingChild;
            } else {
                child = current.createNewSubToken(packetToken, tokenPool);
            }
            current.addSubToken(packetToken);
            current = child;

        } while(child != null);
    }

    public synchronized String toString() {
        return root.deepToString();
    }
}
