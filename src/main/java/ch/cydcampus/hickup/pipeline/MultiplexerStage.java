package ch.cydcampus.hickup.pipeline;

import java.util.HashMap;

/*
 * When an abstraction is passed to be added, it calculates its identifier based on the rule that is passed. 
 * Afterwards, it retreives the child stages. If not present, it creates all child stages for this node. 
 * It then prepends the childStages to the nextStages field of the abstraction. 
 * (Note that before an abstraction is passed to a stage the stage it is passed to is popped from the nextStages list).
 */
public class MultiplexerStage  implements Stage {

    private MultiplexerRule rule;
    private HashMap<String, Stage[]> childStages;

    @Override
    public void process(Abstraction abstraction) {
        
    }
    
    
}
