package ch.cydcampus.hickup.view;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

import com.hickup.points.IPPoint;

import ch.cydcampus.hickup.model.DataModel;
import ch.cydcampus.hickup.model.Token;
import ch.cydcampus.hickup.util.TimeInterval;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class TimelineCanvas extends Canvas {

    public static enum Form {
        CIRCLE, SQUARE, TRIANGLE, DIAMOND, CROSS, PLUS, STAR, INTERVAL
    }

    public static final int PACKET_LAYER_SIZE = 5;
    public static final int BURST_LAYER_SIZE = 7;
    public static final int OBJECT_BURST_LAYER_SIZE = 7;
    public static final int FLOW_INTERACTION_LAYER_SIZE = 3;
    public static final int INTERACTION_LAYER_SIZE = 4;
    public static final int DISCUSSION_LAYER_SIZE = 5;
    public static final int ACTIVITY_LAYER_SIZE = 10;

    boolean didHover = false;
    double maxVal = 24;
    String filter;
    String observedHostsPrefix = "";
    String observedHost = "";
    String hostToHostFilter = "";
    boolean isPlaying = false;
    boolean portDistinction = false;
    boolean isInitialized = false;
    long timeInterval = 100000000L; // 100 seconds
    long current = System.currentTimeMillis() * 1000; // set to current time in microseconds every time draw is called
    DataModel model;
    boolean showActivityLayer = true;
    boolean showPacketLayer = false;
    boolean showBurstLayer = false;
    boolean showObjectBurstLayer = false;
    boolean showFlowInteractionLayer = false;
    boolean showInteractionLayer = true;
    boolean showDiscussionLayer = false;
    double startXDrag = 0;
    long startTimeOnDrag = 0;
    Token hoveredToken = null;
    double mouseX = 0;
    double mouseY = 0;

    String[] timeLegends = new String[] {"1μs", "10μs", "100μs", "1ms", "10ms", "100ms", "1s", "10s", "100s", "1000s", "10000s", "100000s", "1000000s"};
    Long[] timeLegendsMicros = new Long[] {1L, 10L, 100L, 1000L, 10000L, 100000L, 1000000L, 10000000L, 100000000L, 1000000000L, 10000000000L, 100000000000L, 1000000000000L};



    public void togglePortDistinction() {
        portDistinction = !portDistinction;
    }

    public TimelineCanvas(DataModel model) {
        super();
        this.model = model;
    }

    public void setObservedHostsPrefix(String prefix) {
        this.observedHostsPrefix = prefix;
    }

    public void resetObservedHostsPrefix() {
        this.observedHostsPrefix = "";
    }

    public void setHostToHostFilter(String hostToHostFilter) {
        this.hostToHostFilter = hostToHostFilter;
    }

    public void resetHostToHostFilter() {
        this.hostToHostFilter = "";
    }

    public void setObservedHost(String observedHost) {
        this.observedHost = observedHost;
    }

    public void resetObservedHost() {
        this.observedHost = "";
    }

    private void drawBackground(GraphicsContext g) {
        g.clearRect(0, 0, getWidth(), getHeight());
        g.setFill(Color.web("#222222"));
        g.fillRect(0, 0, getWidth(), getHeight());

        // draw x axis
        g.setStroke(Color.web("#000000"));
        g.setLineWidth(2);
        g.strokeLine(0, getHeight()/2, getWidth(), getHeight()/2);        

        // vertical line in the center
        g.setStroke(Color.web("#000000"));
        g.setLineWidth(0.5);
        g.strokeLine(getWidth()/2, 0, getWidth()/2, getHeight());
        g.setLineWidth(1);

        // draw time ticks
        if(isInitialized) {
            Token root = model.getRoot();
            TimeInterval dataTimeInterval = root.getTimeInterval();

            // draw bar at the bottom indicating the time interval shown in the canvas
            g.setStroke(Color.web("#CCCCCC"));
            g.strokeLine(0, getHeight() - 20, getWidth(), getHeight() - 20);
            // use dark gray
            g.setFill(Color.web("#AAAAAA"));
            // fill a rect corresponding to the current window.
            g.fillRect(getWidth() * (current - timeInterval - dataTimeInterval.getStart()) / (dataTimeInterval.getEnd() - dataTimeInterval.getStart()), 
                getHeight() - 20, getWidth() * timeInterval / (dataTimeInterval.getEnd() - dataTimeInterval.getStart()), 20);

            // on top of bar, write at 5 positions the time corresponding to the position. Transform the time in nanoseconds to utf format
            g.setFill(Color.web("#FFFFFF"));

            String time = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS").format(
                TimeInterval.microToInstant((2 * current - timeInterval) / 2).atZone(ZoneOffset.UTC));
            // draw a tick mark on top of the time interval bar
            g.strokeLine(getWidth() / 2, getHeight() - 20, getWidth() / 2, getHeight() - 25);         
            // write the time
            g.fillText(time, getWidth() / 2 - 90, getHeight() - 30);
            
            // draw time legend in bottom left
            int legendIndex = (int) Math.log10(timeInterval) - 1;
            if(legendIndex >= timeLegends.length) {
                legendIndex = timeLegends.length - 1;
            }
            g.fillText(timeLegends[legendIndex], 10, getHeight() - 45);
            g.strokeLine(10, getHeight() - 30, 10, getHeight() - 36);
            double timeLegendWidth = getWidth() * timeLegendsMicros[legendIndex] / timeInterval;
            g.strokeLine(10, getHeight() - 33, 10 + timeLegendWidth, getHeight() - 33);
            g.strokeLine(10 + timeLegendWidth, getHeight() - 30, 10 + timeLegendWidth, getHeight() - 36);

            // draw two ticks next to middle
            g.setLineWidth(0.5);
            double x = timeLegendWidth / 2;
            while(x < getWidth() / 2) {
                g.strokeLine(getWidth() / 2 - x, getHeight() / 2 - 3, getWidth() / 2 - x, getHeight() / 2 + 3);       
                g.strokeLine(getWidth() / 2 + x, getHeight() / 2 - 3, getWidth() / 2 + x, getHeight() / 2 + 3);  
                x += timeLegendWidth;      
            }
 
        }
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
        if(portDistinction) {
            return Color.hsb(Math.abs(t.getState().getBidirectionalFlowIdentifier().hashCode()) % 360, 1, 1);
        }

        return Color.hsb(Math.abs(t.getState().getHostToHostIdentifier().hashCode()) % 360, 1, 1);
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
        if(observedHostsPrefix.length() > 0 && srcIp.startsWith(observedHostsPrefix)) {
            return getHeight() / 2 + getHeight() * Math.log(bytes) / maxVal / 2;
        }

        // default also if no observed host group selected
        return getHeight() / 2 - getHeight() * Math.log(bytes) / maxVal / 2;
    }

    private void checkHover(Token t, Form form, double size) {
        double x = getX(t);
        double y = getY(t);

        if(form == Form.CIRCLE) {
            // circle bounding box with pythagoras
            if(Math.pow(x - mouseX, 2) + Math.pow(y - mouseY, 2) <= Math.pow(size / 2, 2)) {
                hoveredToken = t;
                return;
            }            
        } else if(form == Form.TRIANGLE) {
            // triangle bounding box with line side test
            double x1 = x - size / 2;
            double x2 = x + size / 2;
            double x3 = x;
            double y1 = y + size / 2;
            double y2 = y + size / 2;
            double y3 = y - size / 2;
            // Calculate the barycentric coordinates
            double denominator = (y2 - y3) * (x1 - x3) + (x3 - x2) * (y1 - y3);
            double alpha = ((y2 - y3) * (mouseX - x3) + (x3 - x2) * (mouseY - y3)) / denominator;
            double beta = ((y3 - y1) * (mouseX - x3) + (x1 - x3) * (mouseY - y3)) / denominator;
            double gamma = 1 - alpha - beta;

            // Check if the mouse is inside the triangle
            if(alpha > 0 && beta > 0 && gamma > 0) {
                hoveredToken = t;
                return;
            }

        } else if(form == Form.SQUARE) {
            // square bounding box
            if(Math.abs(x - mouseX) <= size / 2 && Math.abs(y - mouseY) <= size / 2) {
                hoveredToken = t;
                return;
            }
        } else if(form == Form.INTERVAL) {
            // interval bounding box
            TimeInterval interval = t.getTimeInterval();
            double x1 = getX(interval.getStart());
            double x2 = getX(interval.getEnd());

            if(x1 == x2) {
                x1 -= 1;
                x2 += 1;
            }

            double y1 = getY(t) - size / 2;
            double y2 = y1 + size;
            if(x1 <= mouseX && mouseX <= x2 && y1 <= mouseY && mouseY <= y2) {
                hoveredToken = t;
                return;
            }
        } else {
            // don't check for other forms
        }
    }

    private void drawToken(GraphicsContext g, Token t) {
        if(t.getLevel() == Token.PACKET_LAYER && showPacketLayer) {
            drawForm(g, Form.CIRCLE, t, PACKET_LAYER_SIZE);
            checkHover(t, Form.CIRCLE, PACKET_LAYER_SIZE);
        } else if(t.getLevel() == Token.BURST_LAYER && showBurstLayer) {
            drawForm(g, Form.TRIANGLE, t, BURST_LAYER_SIZE);
            checkHover(t, Form.TRIANGLE, BURST_LAYER_SIZE);
        } else if(t.getLevel() == Token.OBJECT_BURST_LAYER && showObjectBurstLayer) {
            drawForm(g, Form.SQUARE, t, OBJECT_BURST_LAYER_SIZE);
            checkHover(t, Form.SQUARE, OBJECT_BURST_LAYER_SIZE);
        } else if(t.getLevel() == Token.FLOW_INTERACTION_LAYER && showFlowInteractionLayer) {
            drawInterval(g, t, FLOW_INTERACTION_LAYER_SIZE);
            checkHover(t, Form.INTERVAL, FLOW_INTERACTION_LAYER_SIZE);
        } else if(t.getLevel() == Token.INTERACTION_LAYER && showInteractionLayer) {
            drawInterval(g, t, INTERACTION_LAYER_SIZE);
            checkHover(t, Form.INTERVAL, INTERACTION_LAYER_SIZE);
        } else if(t.getLevel() == Token.DISCUSSION_LAYER && showDiscussionLayer) {
            drawInterval(g, t, DISCUSSION_LAYER_SIZE);
            checkHover(t, Form.INTERVAL, DISCUSSION_LAYER_SIZE);
        } else if(t.getLevel() == Token.ACTIVITY_LAYER && showActivityLayer) {
            drawInterval(g, t, ACTIVITY_LAYER_SIZE);
            checkHover(t, Form.DIAMOND, ACTIVITY_LAYER_SIZE);
        } else {
            // don't draw anything
        }
    }

    private void drawData(GraphicsContext g) {

        Token root = model.getRoot();
        int maxLayer = getMaxLayer();
        if(!isInitialized && root.getState().getNumSubTokens() > 0) {
            isInitialized = true;
            current = root.getTimeInterval().getStart() + timeInterval;
        }

        Queue<Collection<Token>> bfsQueue = new LinkedList<>();

        bfsQueue.add(root.getSubTokens());

        TimeInterval currentInterval = new TimeInterval(current - timeInterval, current);
        while(!bfsQueue.isEmpty()) {
            Collection<Token> tokens = bfsQueue.remove();
            for(Token t : tokens) {
                // only take traffic including observedHost
                if(observedHost.length() > 0 && 
                    !t.getState().getSrcIP().equals(observedHost) && 
                    !t.getState().getDstIP().equals(observedHost)) {
                    continue;
                }

                // if token is on discussion layer, only take it if traffic egresses or ingresses
                if(t.getLevel() == Token.DISCUSSION_LAYER && 
                    observedHostsPrefix.length() > 0 &&
                    !(t.getState().getSrcIP().startsWith(observedHostsPrefix) || 
                    t.getState().getDstIP().startsWith(observedHostsPrefix))) {
                    continue;
                }

                // only take traffic that is between hosts in hostToHostFilter
                if(t.getLevel() == Token.DISCUSSION_LAYER && 
                    hostToHostFilter.length() > 0 &&
                    !t.getState().getHostToHostIdentifier().equals(hostToHostFilter)) {
                    continue;
                }

                // only take traffic that is visible in the current time interval
                if(t.getTimeInterval().doIntersect(currentInterval)) {
                    drawToken(g, t);
                    if(t.getLevel() < maxLayer) {
                        bfsQueue.add(t.getSubTokens());
                    }
                }
            }
        }
    }

    public void togglePlayMode() {
        if(isPlaying) {
            isPlaying = false;
        } else {
            isPlaying = true;
        }        
    }

    public void setPlayingMode(boolean playingMode) {
        this.isPlaying = playingMode;
    }

    private int getMaxLayer() {
        if(showPacketLayer) {
            return Token.PACKET_LAYER;
        } else if(showBurstLayer) {
            return Token.BURST_LAYER;
        } else if(showObjectBurstLayer) {
            return Token.OBJECT_BURST_LAYER;
        } else if(showFlowInteractionLayer) {
            return Token.FLOW_INTERACTION_LAYER;
        } else if(showInteractionLayer) {
            return Token.INTERACTION_LAYER;
        } else {
            return Token.DISCUSSION_LAYER;
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

        if(filter.contains("0")) {
            showActivityLayer = true;
        } else {
            showActivityLayer = false;
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
        // cap min and max time interval
        if(timeInterval < 10L) {
            timeInterval = 10L;
        } else if(timeInterval > 100000000000000L) {
            timeInterval = 100000000000000L;
        }
        current = mid + timeInterval / 2;
    }

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
        hoveredToken = null;
        if(isPlaying) {
            current = System.currentTimeMillis() * 1000;
        }
        
        GraphicsContext gc = getGraphicsContext2D();
        drawBackground(gc);
        drawData(gc);
    }

    public void handleMouseMoved(double x, double y) {
        mouseX = x;
        mouseY = y;
        didHover = true;
    }

    public Token getHoveredToken() {
        return hoveredToken;
    }
}
