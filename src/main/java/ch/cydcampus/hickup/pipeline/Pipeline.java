package ch.cydcampus.hickup.pipeline;

public class Pipeline {

    private AbstractionDeque[] abstractionDeques;
    private long logicalTime; // TODO: add suport for real time


    public Pipeline() {
        abstractionDeques = new AbstractionDeque[4];
    }

    public void run() {

        while(true) { // main loop

            for(int i = 0; i < abstractionDeques.length; i++) {
                Abstraction abstraction = abstractionDeques[i].getFirstAbstraction(logicalTime); // logical time no influence on level 0
                if(abstraction == null) {
                    continue;
                }

                if(abstraction.getLevel() == 0) {
                    logicalTime = Math.max(abstraction.getLastUpdateTime(), logicalTime);
                }

                processAbstraction(abstraction, i);
            }

        }

    }

    private void processAbstraction(Abstraction abstraction, int level) {

        // Compute combination features

    }
    
}
