package ch.cydcampus.hickup.model;

public class SimpleCombinationRule implements CombinationRule {

    public static final long INTERACTION_TIME_THRESHOLD = 1000000L; // 1 second
    public static final long OBJECT_BURST_TIME_THRESHOLD = 100000L; // 100 milliseconds
    public static final long BURST_TIME_THRESHOLD = 1000L; // 1 millisecond

    @Override
    public boolean belongsToToken(Token decisionToken, Token newToken) {

        TokenState decisionTokenState = decisionToken.getState();
        TokenState newTokenState = newToken.getState();
        switch (decisionToken.getLevel()) { // we never check the root token
            case 1: // at host-to-host tokens
                return decisionToken.getTimeInterval()
                    .getDifference(newToken.getTimeInterval()) < INTERACTION_TIME_THRESHOLD;
            case 2: // at interaction tokens
                return newTokenState.getBidirectionalFlowIdentifier()
                    .equals(decisionTokenState.getBidirectionalFlowIdentifier());
            case 3: // at flow interaction tokens
                return true; // no combination rule.
            case 4: // at object burst tokens
                return decisionToken.getTimeInterval()
                    .getDifference(newToken.getTimeInterval()) < OBJECT_BURST_TIME_THRESHOLD &&
                    decisionTokenState.getDstIP().equals(newTokenState.getDstIP());
            case 5: // at burst tokens
                return decisionToken.getTimeInterval()
                    .getDifference(newToken.getTimeInterval()) < BURST_TIME_THRESHOLD && 
                    decisionTokenState.getDstIP().equals(newTokenState.getDstIP());
            default: // at packet tokens. Should not have any children, so don't combine.
                return false;
        }

    }
    
}
