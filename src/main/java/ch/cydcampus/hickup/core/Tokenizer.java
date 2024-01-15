package ch.cydcampus.hickup.core;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import ch.cydcampus.hickup.core.abstraction.Abstraction;
import ch.cydcampus.hickup.core.abstraction.SpatialAbstraction;
import ch.cydcampus.hickup.core.abstraction.TemporalAbstraction;

public class Tokenizer {

    private static final String[] CONNECTORS = {" ", "", "-", "*", "/", "(", ")", "[", "]", "{", "}", "<", ">", "=", "!", "&", "|", "^", "~", "%", "@", "#", "$", "?", ":", ";", ",", ".", "_", " "};
    private static final String[] SIZE_TOKENS = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j",
                                                "k", "l", "m", "n", "o", "p", "q", "r", "s", "t",
                                                "u", "v", "w", "x", "y", "z",
                                                "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", 
                                                "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
                                                "U", "V", "W", "X", "Y", "Z"};
    private static final double SIZE_TOKENS_LOG_LOWER_BOUND = 5;
    private static final double SIZE_TOKENS_LOG_UPPER_BOUND = 17;
    private static final String OUTPUT_PATH = "output/";

    private Abstraction root;
    private int tokenLevel;

    public Tokenizer(Abstraction root, int tokenLevel) {
        this.root = root;
        this.tokenLevel = tokenLevel;
        System.out.println(root);
    }

    public void tokenize() {
        tokenize(root, 0);
    }

    private String getSizeToken(long bytes) {
        double logBytes = Math.log(bytes);

        if(logBytes < SIZE_TOKENS_LOG_LOWER_BOUND) {
            return SIZE_TOKENS[0];
        } else if(logBytes > SIZE_TOKENS_LOG_UPPER_BOUND) {
            return SIZE_TOKENS[SIZE_TOKENS.length - 1];
        } else {
            int index = (int) Math.round((logBytes - SIZE_TOKENS_LOG_LOWER_BOUND) / (SIZE_TOKENS_LOG_UPPER_BOUND - SIZE_TOKENS_LOG_LOWER_BOUND) * (SIZE_TOKENS.length - 1));
            return SIZE_TOKENS[index];
        }
    }

    private void tokenize(Abstraction abstraction, int connectorIndex) {
        int level = abstraction.getLayer();
        if(level >= tokenLevel) {
            System.out.print(getSizeToken(abstraction.getBytes()));
            return;
        }

        if(abstraction instanceof SpatialAbstraction) {
            SpatialAbstraction spatialAbstraction = (SpatialAbstraction) abstraction;
            HashMap<Integer, ConcurrentHashMap<String, Abstraction>> children = spatialAbstraction.getChildren();
            for(int childLayer : children.keySet()) {
                for(String key : children.get(childLayer).keySet()) {
                    tokenize(children.get(childLayer).get(key), connectorIndex + 1);
                    System.out.print(CONNECTORS[connectorIndex]);
                }
            }
        } else if(abstraction instanceof TemporalAbstraction) {
            TemporalAbstraction temporalAbstraction = (TemporalAbstraction) abstraction;
            for(Abstraction a : temporalAbstraction.getChildren()) {
                tokenize(a, connectorIndex + 1);
                System.out.print(CONNECTORS[connectorIndex]);
            }
            if(temporalAbstraction.getChildren().size() > 0) {
                connectorIndex++;
            }
        }

    }


}
