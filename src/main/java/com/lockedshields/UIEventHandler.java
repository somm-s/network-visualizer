package com.lockedshields;

import com.hickup.points.IPPoint;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

public class UIEventHandler implements EventHandler<Event>{

    DataBuffer dataBuffer;
    TimelineCanvas canvas;

    double startXDrag = 0;
    long startTimeOnDrag = 0;

    public UIEventHandler(DataBuffer dataBuffer, TimelineCanvas canvas) {
        this.dataBuffer = dataBuffer;
        this.canvas = canvas;

        canvas.setOnMouseMoved(this::handleMouseMoved);
    }

    @Override
    public void handle(Event event) {
        if (event instanceof MouseEvent) {
            handleMouseEvent((MouseEvent) event);
        } else if (event instanceof ScrollEvent) {
            handleScrollEvent((ScrollEvent) event);
        }
    }

    private void handleMouseEvent(MouseEvent event) {
        if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
            startXDrag = event.getX();
            startTimeOnDrag = dataBuffer.startTime;

        } else if (event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
            double delta = event.getX() - startXDrag;
            // adjust start time
            long startTime = (long) (startTimeOnDrag - delta * dataBuffer.timeInterval / canvas.getWidth());
            dataBuffer.setStartTime(startTime);
        }
    }

    private void handleMouseMoved(MouseEvent event) {
        // Get the coordinates of the mouse pointer
        double mouseX = event.getX();
        double mouseY = event.getY();

        // Find the hovered point in the model based on the mouse coordinates
        IPPoint hoveredPoint = dataBuffer.getHoveredPoint(mouseX, mouseY);

        // Update the information panel with information about the hovered point
        if (hoveredPoint != null) {
            // String info = String.format("Point: %s, Value: %s", hoveredPoint.getMicroseconds(), hoveredPoint.val);
            // infoPanel.updateInfo(info);
            System.out.println("Hovered point: " + hoveredPoint.toString());
        } else {
            // infoPanel.updateInfo("Hover over a point to see information");
            // System.out.println("Hovered point: null");
        }
    }


    private void handleScrollEvent(ScrollEvent event) {
        double deltaY = event.getDeltaY();
        dataBuffer.setScrollDelta(deltaY);
    }    
}
