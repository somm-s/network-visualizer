package com.lockedshields;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

public class UIEventHandler implements EventHandler<Event>{

    DataBuffer dataBuffer;
    TimelineCanvas canvas;

    double startXDrag = 0;
    long startTimeOnDrag = 0;

    private boolean isCanvasActive = false;

    public UIEventHandler(DataBuffer dataBuffer, TimelineCanvas canvas) {
        this.dataBuffer = dataBuffer;
        this.canvas = canvas;
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

        } else if (event.getEventType() == MouseEvent.MOUSE_ENTERED) {
            // check if mouse entered canvas
            if (event.getTarget() == canvas) {
                isCanvasActive = true;
            }

        } else if (event.getEventType() == MouseEvent.MOUSE_EXITED) {
            // check if mouse exited canvas
            if (event.getTarget() == canvas) {
                isCanvasActive = false;
            }
            
        }
    }

    private void handleScrollEvent(ScrollEvent event) {
        double deltaY = event.getDeltaY();
        dataBuffer.setScrollDelta(deltaY);
    }    
}
