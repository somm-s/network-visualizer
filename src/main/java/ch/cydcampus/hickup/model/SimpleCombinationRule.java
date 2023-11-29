package ch.cydcampus.hickup.model;

public class SimpleCombinationRule implements CombinationRule {

    public static final long INTERACTION_TIME_THRESHOLD = 1000000L; // 1 second
    public static final long OBJECT_BURST_TIME_THRESHOLD = 100000L; // 100 milliseconds
    public static final long BURST_TIME_THRESHOLD = 1000L; // 1 millisecond

    @Override
    public boolean belongsToToken(Token decisionToken, Token newToken) {

        TokenState decisionTokenState = decisionToken.getState();
        TokenState newTokenState = newToken.getState();
        switch (decisionToken.getLevel()) {
            case 1: // at root
                return newTokenState.getHostToHostIdentifier()
                    .equals(decisionTokenState.getHostToHostIdentifier());
            case 2: // at host-to-host tokens
                return decisionToken.getTimeInterval()
                    .getDifference(newToken.getTimeInterval()) < INTERACTION_TIME_THRESHOLD;
            case 3: // at interaction tokens
                return newTokenState.getBidirectionalFlowIdentifier()
                    .equals(decisionTokenState.getBidirectionalFlowIdentifier());
            case 4: // at flow interaction tokens
                return true; // no combination rule.
            case 5: // at object burst tokens
                return decisionToken.getTimeInterval()
                    .getDifference(newToken.getTimeInterval()) < OBJECT_BURST_TIME_THRESHOLD;
            case 6: // at burst tokens
                return decisionToken.getTimeInterval()
                    .getDifference(newToken.getTimeInterval()) < BURST_TIME_THRESHOLD;
            default: // at packet tokens. Should not have any children.
                throw new UnsupportedOperationException("Level " + decisionToken.getLevel() + " not available");
        }

    }
    
}
