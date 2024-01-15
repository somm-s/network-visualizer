package ch.cydcampus.hickup.pipeline;

public class HighOrderAbstractionDeque implements AbstractionDeque {

    private int level; // level of abstraction, 0 = packet data.
    private int size;
    private long timeout; // timeout in microseconds
    private Abstraction first;
    private Abstraction last;

    public HighOrderAbstractionDeque(int level, long timeout) {
        this.level = level;
        this.timeout = timeout;
        this.size = 0;
        this.first = null;
        this.last = null;
    }
    
    /*
     * Adds an abstraction to the deque. If the abstraction is already in the deque, it is moved to the end.
     * It is assumed that the abstraction is active, i.e., the timeout is reset.
     */
    public void addAbstraction(Abstraction abstraction) {
        // Case I: Deque is empty
        if(isEmpty()) {
            first = abstraction;
            last = abstraction;
            size++;
            return;
        }

        // Case II
        if(last == abstraction) {
            // already last, nothing to do
            return;
        }

        // Case III
        if(!contains(abstraction)) {
            last.setPrev(abstraction);
            abstraction.setNext(last);
            last = abstraction;
            size++;
            return;
        }

        // Case IV
        if(first == abstraction) {
            assert size > 1; // Otherwise last == first == abstraction --> Case II
            first = abstraction.getPrev();
            first.setNext(null);
            last.setPrev(abstraction);
            abstraction.setNext(last);
            abstraction.setPrev(null);
            last = abstraction;
            return;
        }

        // Case V
        assert abstraction.getNext() != null && abstraction.getPrev() != null;
        abstraction.getPrev().setNext(abstraction.getNext());
        abstraction.getNext().setPrev(abstraction.getPrev());
        last.setPrev(abstraction);
        abstraction.setNext(last);
        abstraction.setPrev(null);
        last = abstraction;
    }

    /*
     * Returns the first abstraction from the deque if it is sealed, i.e., the timeout has expired.
     * Removes the abstraction from the deque. Returns null if the first abstraction is not sealed.
     */
    public Abstraction getFirstAbstraction(long currentTime) {
        if(isEmpty()) {
            return null;
        }

        if(first.getLastUpdateTime() + timeout < currentTime) {
            Abstraction abstraction = first;
            first = first.getPrev();
            if(first != null) {
                first.setNext(null);
            }
            abstraction.setPrev(null);
            assert abstraction.getNext() == null;
            size--;
            return abstraction;
        }

        return null;
    }

    public int getSize() {
        return size;
    }

    public boolean isEmpty() {
        return first == null;
    }

    /*
     * Returns true if the abstraction is in the deque.
     * There is exactly one deque per level. An abstraction that is not in the deque always
     * has both next and prev set to null. An abstraction that is in the deque always has
     * either next or prev set to an abstraction that is also in the deque or is the only
     * abstraction in the deque (where first points to the abstraction).
     */
    private boolean contains(Abstraction abstraction) {
        if(abstraction.getLevel() != level) {
            return false;
        }

        if(abstraction.getNext() != null || abstraction.getPrev() != null || first == abstraction) {
            return true;
        }

        return false;
    }
}
