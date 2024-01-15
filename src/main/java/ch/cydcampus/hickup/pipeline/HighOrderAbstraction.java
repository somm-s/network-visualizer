package ch.cydcampus.hickup.pipeline;

import java.util.List;

import ch.cydcampus.hickup.pipeline.feature.Feature;

public class HighOrderAbstraction implements Abstraction {

    private int level;
    private long timeout;
    private long lastUpdateTime;
    private List<Abstraction> children;
    private Abstraction activeAbstraction;
    private Abstraction next;
    private Abstraction prev;
    private Feature[] features;

    public HighOrderAbstraction(int level, long timeout) {
        this.level = level;
        this.timeout = timeout;
        this.lastUpdateTime = 0;
        this.children = null;
        this.activeAbstraction = null;
        this.next = null;
        this.prev = null;
    }

    @Override
    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public List<Abstraction> getChildren() {
        return children;
    }

    @Override
    public void addChild(Abstraction abstraction) {
        children.add(abstraction);
    }

    @Override
    public Abstraction getActiveAbstraction() {
        return activeAbstraction;
    }

    @Override
    public Abstraction getNext() {
        return next;
    }

    @Override
    public Abstraction getPrev() {
        return prev;
    }

    @Override
    public void setNext(Abstraction abstraction) {
        next = abstraction;
    }

    @Override
    public void setPrev(Abstraction abstraction) {
        prev = abstraction;
    }

    @Override
    public void addFeatures(Feature[] features) {
        this.features = features;
    }

    @Override
    public Feature[] getFeatures() {
        return features;
    }

}
