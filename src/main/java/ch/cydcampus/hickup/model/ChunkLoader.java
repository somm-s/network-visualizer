package ch.cydcampus.hickup.model;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import ch.cydcampus.hickup.util.TimeInterval;

/*
 * Requests and releases chunks of tokens from and to the IOModule.
 * Runs IOModule code to load chunks from DB.
 * Serves data requests from clients and runs in a separate thread.
 */
public class ChunkLoader {
    
    TimeInterval chunkInterval;
    DataModel model;
    LinkedList<Token> content = new LinkedList<>();

    public ChunkLoader(DataModel dataModel, TimeInterval chunkInterval) {
        this.model = dataModel;
        this.chunkInterval = chunkInterval;
    }

    public TimeInterval getChunkInterval() {
        return chunkInterval;
    }

    public void setChunkInterval(TimeInterval chunkInterval) {
        int count = 0;
        this.chunkInterval = chunkInterval;
        Token root = model.getRoot();
        Queue<Collection<Token>> bfsQueue = new LinkedList<>();
        bfsQueue.add(root.getSubTokens());

        while(!bfsQueue.isEmpty()) {
            Collection<Token> tokens = bfsQueue.remove();
            for(Token t : tokens) {

                if(t.getLevel() == Token.INTERACTION_LAYER) {
                    if(!t.getTimeInterval().doIntersect(chunkInterval)) {
                        continue;
                    }
                }
                count++;
                content.add(t);
                if(t.getLevel() < Token.PACKET_LAYER) {
                    bfsQueue.add(t.getSubTokens());
                }
            }
        }
        System.out.println("Went over " + count + " tokens.");
    }

    public Iterator<Token> getIterator() {
        return content.iterator();
    }

}
