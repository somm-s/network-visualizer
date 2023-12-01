package ch.cydcampus.hickup;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

import ch.cydcampus.hickup.model.DataModel;
import ch.cydcampus.hickup.model.Token;
import ch.cydcampus.hickup.util.TimeInterval;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class TimelineCanvas extends Canvas {

    double maxVal = 24;
    String filter = "";
    boolean isPlaying = true;
    long timeInterval = 100000000L; // 10 seconds
    long current = System.currentTimeMillis() * 1000; // set to current time in microseconds every time draw is called
    DataModel model;
    boolean showPacketLayer = true; // 6
    boolean showBurstLayer = true; // 5
    boolean showObjectBurstLayer = true; // 4
    boolean showFlowInteractionLayer = true; // 3
    boolean showInteractionLayer = true; // 2
    boolean showDiscussionLayer = true; // 1
    double startXDrag = 0;
    long startTimeOnDrag = 0;

    public static enum Form {
        CIRCLE, SQUARE, TRIANGLE, DIAMOND, CROSS, PLUS, STAR
    }

    String observedHost = "192.168.200.29";

    public TimelineCanvas(DataModel model) {
        super();
        this.model = model;
    }

    private void drawBackground(GraphicsContext g) {
        g.clearRect(0, 0, getWidth(), getHeight());
        g.setFill(Color.web("#222222"));
        g.fillRect(0, 0, getWidth(), getHeight());

        // draw x axis
        g.setStroke(Color.web("#000000"));
        g.strokeLine(0, getHeight()/2, getWidth(), getHeight()/2);        
    }

    private void drawInterval(GraphicsContext g, Token t, double lw) {
        TimeInterval interval = t.getTimeInterval();
        double x1 = getX(interval.getStart());
        double x2 = getX(interval.getEnd());
        double y1 = getY(t);
        double y2 = y1;
        g.setLineWidth(lw);
        g.setStroke(getHostColor(t));
        g.strokeLine(x1, y1, x2, y2);
        g.setLineWidth(1);
    }

    private void drawForm(GraphicsContext g, Form form, Token t, double size) {
        double x = getX(t);
        double y = getY(t);
        Color c = getHostColor(t);
        g.setFill(c);
        if(form == Form.CIRCLE) {
            g.fillOval(x - size / 2, y - size / 2, size, size);
        } else if(form == Form.SQUARE) {
            g.fillRect(x - size / 2, y - size / 2, size, size);
        } else if(form == Form.TRIANGLE) {
            g.fillPolygon(new double[] {x - size / 2, x + size / 2, x}, new double[] {y + size / 2, y + size / 2, y - size / 2}, 3);
        } else if(form == Form.DIAMOND) {
            g.fillPolygon(new double[] {x - size / 2, x, x + size / 2, x}, new double[] {y, y - size / 2, y, y + size / 2}, 4);
        } else if(form == Form.CROSS) {
            g.strokeLine(x - size / 2, y - size / 2, x + size / 2, y + size / 2);
            g.strokeLine(x - size / 2, y + size / 2, x + size / 2, y - size / 2);
        } else if(form == Form.PLUS) {
            g.strokeLine(x - size / 2, y, x + size / 2, y);
            g.strokeLine(x, y - size / 2, x, y + size / 2);
        } else if(form == Form.STAR) {
            g.strokeLine(x - size / 2, y - size / 2, x + size / 2, y + size / 2);
            g.strokeLine(x - size / 2, y + size / 2, x + size / 2, y - size / 2);
            g.strokeLine(x - size / 2, y, x + size / 2, y);
            g.strokeLine(x, y - size / 2, x, y + size / 2);
        }
    } 

    private Color getHostColor(Token t) {
        if(t.getState().getSrcIP().equals(observedHost)) {
            return Color.hsb(Math.abs(t.getState().getDstIP().hashCode()) % 360, 1, 1);
        } else {
            return Color.hsb(Math.abs(t.getState().getSrcIP().hashCode()) % 360, 1, 1);
        }
    }

    private double getX(long time) {
        double xFraction = (current - time) / (double) timeInterval;
        return getWidth() * (1 - xFraction);
    }

    private double getX(Token t) {
        TimeInterval interval = t.getTimeInterval();
        long avg = interval.getStart() + (interval.getEnd() - interval.getStart()) / 2;
        return getX(avg);
    }

    private double getY(Token t) {
        long bytes = t.getState().getBytes();
        String srcIp = t.getState().getSrcIP();
        if(srcIp.equals(observedHost)) {
            return getHeight() / 2 + getHeight() * Math.log(bytes) / maxVal / 2;
        } else {
            return getHeight() / 2 - getHeight() * Math.log(bytes) / maxVal / 2;
        }
    }

    private void drawToken(GraphicsContext g, Token t) {

        if(t.getLevel() == Token.PACKET_LAYER && showPacketLayer) {
            drawForm(g, Form.CIRCLE, t, 2);
        } else if(t.getLevel() == Token.BURST_LAYER && showBurstLayer) {
            drawForm(g, Form.SQUARE, t, 5);
        } else if(t.getLevel() == Token.OBJECT_BURST_LAYER && showObjectBurstLayer) {
            drawForm(g, Form.TRIANGLE, t, 6);
        } else if(t.getLevel() == Token.FLOW_INTERACTION_LAYER && showFlowInteractionLayer) {
            drawInterval(g, t, 3);
        } else if(t.getLevel() == Token.INTERACTION_LAYER && showInteractionLayer) {
            drawInterval(g, t, 4);
        } else if(t.getLevel() == Token.DISCUSSION_LAYER && showDiscussionLayer) {
            drawInterval(g, t, 5);
        } else {
            // don't draw anything
        }
    }

    private void drawData(GraphicsContext g) {

        Token root = model.getRoot();
        Queue<Collection<Token>> bfsQueue = new LinkedList<>();

        bfsQueue.add(root.getSubTokens());

        TimeInterval currentInterval = new TimeInterval(current - timeInterval, current);
        while(!bfsQueue.isEmpty()) {
            Collection<Token> tokens = bfsQueue.remove();
            for(Token t : tokens) {
                if(t.getTimeInterval().doIntersect(currentInterval)) {
                    drawToken(g, t);
                    if(t.getLevel() < Token.PACKET_LAYER) {
                        bfsQueue.add(t.getSubTokens());
                    }
                }
            }
        }
    }

    static int i = 0;

    public void togglePlayMode() {
        if(isPlaying) {
            isPlaying = false;
        } else {
            isPlaying = true;
        }        
    }


    public void setFilter(String filter) {
        this.filter = filter;

        if(filter.contains("6")) {
            showPacketLayer = true;
        } else {
            showPacketLayer = false;
        }

        if(filter.contains("5")) {
            showBurstLayer = true;
        } else {
            showBurstLayer = false;
        }

        if(filter.contains("4")) {
            showObjectBurstLayer = true;
        } else {
            showObjectBurstLayer = false;
        }

        if(filter.contains("3")) {
            showFlowInteractionLayer = true;
        } else {
            showFlowInteractionLayer = false;
        }

        if(filter.contains("2")) {
            showInteractionLayer = true;
        } else {
            showInteractionLayer = false;
        }

        if(filter.contains("1")) {
            showDiscussionLayer = true;
        } else {
            showDiscussionLayer = false;
        }

    }

    public void handleScroll(double deltaY) {
        long lastInterval = timeInterval;
        long mid = current - timeInterval / 2;
        if(deltaY > 0) {
            timeInterval =(long) (1.111111 * (double) lastInterval);
        } else if (deltaY < 0) {
            timeInterval = (long) (0.9 * (double) lastInterval);
        }
        current = mid + timeInterval / 2;
    }

    // add listener for drag and drop to control the start time of the data shown in the canvas
    // dragging left or right changes the start time of the data shown in the canvas
    public void handleMousePressed(double x) {
        startXDrag = x;
        startTimeOnDrag = current;
    }

    public void handleMouseDragged(double x) {
        // calculate delta
        double delta = x - startXDrag;
        // adjust start time
        current = (long) ((double) startTimeOnDrag - delta * timeInterval / this.getWidth());
    }


    public void draw() {
        if(isPlaying) {
            current = System.currentTimeMillis() * 1000;
        }
        
        GraphicsContext gc = getGraphicsContext2D();
        drawBackground(gc);
        drawData(gc);
    }
}
