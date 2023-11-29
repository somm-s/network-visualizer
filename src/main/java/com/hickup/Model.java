package com.hickup;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

import com.hickup.points.IPPoint;

public class Model {
    
    private static Model instance = null;
    
    private HashMap<String, PortPair> portPairs = new HashMap<>();

    private Model() {
        
    }
    
    public static Model getInstance() {
        if (instance == null) {
            instance = new Model();
        }
        return instance;
    }

    public void addPoint(IPPoint p) {
        
        String identifier = p.getConnectionIdentifier();
        PortPair portPair = portPairs.get(identifier);
        if (portPair == null) {
            portPair = new PortPair(p.getSrcPort(), p.getDstPort(), p.srcIp, p.dstIp);
            portPairs.put(identifier, portPair);
        }
        portPair.addPoint(p);

    }

    public void writeStats(FileWriter fileWriter) throws IOException {
        for (PortPair portPair : portPairs.values()) {
            
            
        
        }
    }

    public HashMap<String, PortPair> getPortPairs() {
        return portPairs;
    }


    class PortPair {

        private int observerPort;
        private int foreignPort;
        private String observerIP;
        private String foreignIP;

        LinkedList<IPPoint> receivedPoints = new LinkedList<>();
        LinkedList<IPPoint> sentPoints = new LinkedList<>();

        public PortPair(int observerPort, int foreignPort, String observerIP, String foreignIP) {
            this.observerPort = observerPort;
            this.foreignPort = foreignPort;
            this.observerIP = observerIP;
            this.foreignIP = foreignIP;
        }

        public int getObserverPort() {
            return observerPort;
        }

        public int getForeignPort() {
            return foreignPort;
        }

        public String getObserverIP() {
            return observerIP;
        }

        public String getForeignIP() {
            return foreignIP;
        }

        public void addPoint(IPPoint p) {
            if(p.srcIp.equals(observerIP)) {
                sentPoints.add(p);
            } else {
                receivedPoints.add(p);
            }
        }

        public LinkedList<IPPoint> getReceivedPoints() {
            return receivedPoints;
        }

        public LinkedList<IPPoint> getSentPoints() {
            return sentPoints;
        }

    }
}
