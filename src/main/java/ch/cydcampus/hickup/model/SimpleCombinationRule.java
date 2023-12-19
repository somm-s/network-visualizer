package ch.cydcampus.hickup.model;

public class SimpleCombinationRule implements CombinationRule {

    public static final long ACTIVITY_TIME_THRESHOLD = 1000000L; // 1 second
    public static final long INTERACTION_TIME_THRESHOLD = 300000L; // 300 millisecond
    public static final long OBJECT_BURST_TIME_THRESHOLD = 30000L; // 30 milliseconds
    public static final long BURST_TIME_THRESHOLD = 1000L; // 1 millisecond

    @Override
    public boolean belongsToToken(Token decisionToken, Token newToken) {

        TokenState decisionTokenState = decisionToken.getState();
        TokenState newTokenState = newToken.getState();
        switch (decisionToken.getLevel()) { // we never check the root token
            case Token.ACTIVITY_LAYER: // at activity tokens
                return decisionToken.getTimeInterval()
                    .getDifference(newToken.getTimeInterval()) < ACTIVITY_TIME_THRESHOLD;
            case Token.DISCUSSION_LAYER: // at host-to-host tokens
                return true;
            case Token.INTERACTION_LAYER:
                return decisionToken.getTimeInterval()
                    .getDifference(newToken.getTimeInterval()) < INTERACTION_TIME_THRESHOLD;
            case Token.FLOW_INTERACTION_LAYER: // at interaction tokens
                return newTokenState.getBidirectionalFlowIdentifier()
                    .equals(decisionTokenState.getBidirectionalFlowIdentifier());
            case Token.OBJECT_BURST_LAYER: // at object burst tokens
                return decisionToken.getTimeInterval()
                    .getDifference(newToken.getTimeInterval()) < OBJECT_BURST_TIME_THRESHOLD &&
                    decisionTokenState.getDstIP().equals(newTokenState.getDstIP());
            case Token.BURST_LAYER: // at burst tokens
                return decisionToken.getTimeInterval()
                    .getDifference(newToken.getTimeInterval()) < BURST_TIME_THRESHOLD && 
                    decisionTokenState.getDstIP().equals(newTokenState.getDstIP());
            default:
                throw new RuntimeException("Unknown child token level: " + decisionToken.getLevel());
        }

    }
    
}
