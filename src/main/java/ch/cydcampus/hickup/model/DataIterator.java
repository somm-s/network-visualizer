package ch.cydcampus.hickup.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

import ch.cydcampus.hickup.util.Callback;

public class DataIterator {

    private DataModel data;
    private Callback callback;
    private int callbackLayer;

    public DataIterator(DataModel data, Callback callback, int callbackLayer) {
        this.data = data;
        this.callback = callback;
        this.callbackLayer = callbackLayer;
    }

    public void iterate() {
        
        Token root = data.getRoot();
        Queue<Collection<Token>> bfsQueue = new LinkedList<>();
        bfsQueue.add(root.getSubTokens());
        while(!bfsQueue.isEmpty()) {
            Collection<Token> tokens = bfsQueue.remove();
            for(Token t : tokens) {
                if(t.getLevel() < callbackLayer) {
                    bfsQueue.add(t.getSubTokens());
                } else if(t.getLevel() == callbackLayer) {
                    callback.call(t);
                }
            }
        }
    }
    
}
