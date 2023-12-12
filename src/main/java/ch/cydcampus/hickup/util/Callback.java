package ch.cydcampus.hickup.util;

import ch.cydcampus.hickup.model.Token;

/*
 * Callback interface for iterating over data and aggregating it together with DataIterator.
 */
public interface Callback {
    public void call(Token t);
    public Object getResult();
}
