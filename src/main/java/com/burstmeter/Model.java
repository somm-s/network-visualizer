package com.burstmeter;

import java.util.HashMap;
import com.hickup.points.IPPoint;

public class Model {
    

    public static Model instance;
    public static int sizeThreshold = 50; // in bytes
    public static long burstTimeout = 1000; // in microseconds
    public static long interactionTimeout = 1000000; // in microseconds, 1 second


    HashMap<String, Burst> connections;

    private Model() {
        connections = new HashMap<String, Burst>();
    }

    public static Model getInstance() {
        if (instance == null) {
            instance = new Model();
        }
        return instance;
    }

    public void addPoint(IPPoint p) {

        // if point is smaller than size threshold, ignore it
        if (p.packetSize < sizeThreshold) {
            return;
        }

        // get connection identifier
        String connectionIdentifier = p.getConnectionIdentifier();
        // System.out.println("adding point... " + connectionIdentifier);

        // if burst does not exist, create it   
        Burst burst = null;
        if (!connections.containsKey(connectionIdentifier)) {
            burst = new Burst(p);
            connections.put(connectionIdentifier, burst);
        } else {

            burst = connections.get(connectionIdentifier);

            if(!burst.sameBurst(p)) {
                // create new burst
                Burst newBurst = new Burst(p);
                newBurst.setNext(burst);

                if(burst.sameInteraction(p)) {
                    burst.setPrev(newBurst);
                }

                connections.put(connectionIdentifier, newBurst);
                burst = newBurst;
            }

        }

        // add point to burst
        burst.addPoint(p);

    }


    public String toString() {
        String s = "Map size: " + connections.size() + "\n";
        for (String key : connections.keySet()) {
            s += "Key: " + key + "\n";
            s += connections.get(key).toString() + "\n";
        }
        return s;
    }

}
