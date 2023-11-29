package ch.cydcampus.hickup.model;

/*
 * Combination rules that are used to combine the tokens.
 * This interface can be implemented by different combination rules.
 */
public interface CombinationRule {

    public boolean belongsToToken(Token decisionToken, Token newToken);

}
