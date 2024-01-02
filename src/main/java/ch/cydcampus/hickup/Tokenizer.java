package ch.cydcampus.hickup;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.cydcampus.hickup.model.DataModel;
import ch.cydcampus.hickup.model.Token;
import ch.cydcampus.hickup.util.TimeInterval;

public class Tokenizer extends Thread {
    
    // Client is the sender of the first packet of the interaction (request)
    private static final String[] SIZE_TOKENS = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j",
                                                    "k", "l", "m", "n", "o", "p", "q", "r", "s", "t",
                                                    "u", "v", "w", "x", "y", "z",
                                                    "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", 
                                                    "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
                                                    "U", "V", "W", "X", "Y", "Z"};
    private static final double SIZE_TOKENS_LOG_LOWER_BOUND = 5;
    private static final double SIZE_TOKENS_LOG_UPPER_BOUND = 17;
    private static final int MAX_ONE_SIDED_TOKENS = 10;
    private static final String OUTPUT_PATH = "output/";
    private DataModel model;
    private volatile boolean isRunning = true;
    private Map<String, BufferedWriter> streamWriters;

    public Tokenizer(DataModel model) {
        this.model = model;
        this.streamWriters = new HashMap<>();
    }

    private void writeToStream(String streamName, Token token) {
        try {
            BufferedWriter writer = getStreamWriter(streamName);
            writer.write(getTokenString(token));
            writer.flush();
        } catch (IOException e) {
            System.out.println(e);
            e.printStackTrace();
        }
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



    private String getTokenString(Token token) {
        StringBuilder sb = new StringBuilder();

        sb.append("\n");

        List<Token> hostToHostTokens = new ArrayList<>(token.getSubTokens());
        Collections.sort(hostToHostTokens, new Comparator<Token>() {
            @Override
            public int compare(Token o1, Token o2) {
                return o1.getTimeInterval().compareTo(o2.getTimeInterval());
            }
        });

        for(Token hostToHostToken : hostToHostTokens) {
            sb.append(". ");
            
            List<Token> interactionTokens = new ArrayList<>(hostToHostToken.getSubTokens());
            Collections.sort(interactionTokens, new Comparator<Token>() {
                @Override
                public int compare(Token o1, Token o2) {
                    return o1.getTimeInterval().compareTo(o2.getTimeInterval());
                }
            });

            for(Token interactionToken : interactionTokens) {

                List<Token> flowInteractionTokens = new ArrayList<>(interactionToken.getSubTokens());
                Collections.sort(flowInteractionTokens, new Comparator<Token>() {
                    @Override
                    public int compare(Token o1, Token o2) {
                        return o1.getTimeInterval().compareTo(o2.getTimeInterval());
                    }
                });
        
                TimeInterval lastFlowInteractionToken = null;
                TimeInterval latestEndInterval = null;
                for(Token flowInteractionToken : flowInteractionTokens) {
                    if(lastFlowInteractionToken != null) {
                        if(flowInteractionToken.getTimeInterval().getStart() > lastFlowInteractionToken.getEnd()) {
                            sb.append(". ");
                        } else {
                            sb.append(" - ");
                        }
                    }
                    
                    // write object bursts (sequential)
                    List<Token> objectBurstTokens = new ArrayList<>(flowInteractionToken.getSubTokens());
                    Collections.sort(objectBurstTokens, new Comparator<Token>() {
                        @Override
                        public int compare(Token o1, Token o2) {
                            return o1.getTimeInterval().compareTo(o2.getTimeInterval());
                        }
                    });
        
                    String lastDestination = null;
                    int numOneSidedTokens = 0;
                    for(Token objectBurstToken : objectBurstTokens) {
        
                        if(lastDestination != null) {
                            if(!lastDestination.equals(objectBurstToken.getState().getDstIP())) {
                                sb.append(" ");
                                numOneSidedTokens = 0;
                            } else if(numOneSidedTokens >= MAX_ONE_SIDED_TOKENS) {
                                continue; // ignore until next destination
                            }
                        }
        
                        numOneSidedTokens++;
                        sb.append(getSizeToken(objectBurstToken.getState().getBytes()));
                        lastDestination = objectBurstToken.getState().getDstIP();
                    }
        
                    if(latestEndInterval == null || flowInteractionToken.getTimeInterval().getEnd() > latestEndInterval.getEnd()) {
                        latestEndInterval = flowInteractionToken.getTimeInterval();
                    }
                    lastFlowInteractionToken = flowInteractionToken.getTimeInterval();
                }
            }
        }



        return sb.toString();
    }

    private BufferedWriter getStreamWriter(String streamName) throws IOException {
        BufferedWriter writer = streamWriters.get(streamName);
        if (writer == null) {
            writer = new BufferedWriter(new FileWriter(OUTPUT_PATH + streamName + ".txt", false));
            streamWriters.put(streamName, writer);
        }
        return writer;
    }

    public void closeStreams() {
        for (BufferedWriter writer : streamWriters.values()) {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        model.registerReader();

        while(isRunning) {
            try {
                Token token = model.consume();
                writeToStream(token.getState().getHostToHostIdentifier(), token);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        closeStreams();
    }

    public void stopThread() {
        System.out.println("Exiting Tokenizer...");
        isRunning = false;
    }

}
