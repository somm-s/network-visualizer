package ch.cydcampus.hickup.pipeline;

import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import ch.cydcampus.hickup.pipeline.filter.Filter;
import ch.cydcampus.hickup.pipeline.filter.Filter.FilterType;

public abstract class DataSource implements AbstractionDeque {

    private ConcurrentLinkedQueue<Abstraction> queue;

    protected boolean produce(Abstraction abstraction) {
        return queue.offer(abstraction);
    }

    public abstract void setFilter(Filter filter);

    public abstract Set<FilterType> getSupportedFilters();

    @Override
    public Abstraction getFirstAbstraction(long currentTime) {
        return queue.poll();
    }

    @Override
    public void addAbstraction(Abstraction abstraction) {
        throw new UnsupportedOperationException("Not supported for data source.");
    }
    
}
