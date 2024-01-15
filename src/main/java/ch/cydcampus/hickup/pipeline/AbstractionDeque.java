package ch.cydcampus.hickup.pipeline;

public interface AbstractionDeque {
    
    public Abstraction getFirstAbstraction(long currentTime);
    public void addAbstraction(Abstraction abstraction);

}
