package ch.cydcampus.hickup.util;

import java.util.LinkedList;
import java.util.List;

import ch.cydcampus.hickup.model.Token;

/*
 * Collect statistics about a specific layer.
 */
public class LayerStatistics implements Callback {

    private List<Double> statistics;
    private int count;

    public LayerStatistics() {
        this.statistics = new LinkedList<>();
        this.count = 0;
    }

    @Override
    public void call(Token t) {
        statistics.add((double) t.getState().getBytes());
        count++;
    }

    @Override
    public Object getResult() {
        return statistics;
    }

    public int getCount() {
        return count;
    }
    
}
