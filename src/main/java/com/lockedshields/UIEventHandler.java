package com.lockedshields;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

public class UIEventHandler implements EventHandler<Event>{

    DataBuffer dataBuffer;

    long startXDrag = 0;
    long startTimeOnDrag = 0;

    public UIEventHandler(DataBuffer dataBuffer) {
        this.dataBuffer = dataBuffer;
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
            System.out.println("Mouse Pressed");
            // Handle OnMousePressed event
        } else if (event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
            System.out.println("Mouse Dragged");
            // Handle OnMouseDragged event
        }
    }

    private void handleScrollEvent(ScrollEvent event) {
        System.out.println("Scrolling");
        // Handle scrolling and extract deltaY
        double deltaY = event.getDeltaY();
        System.out.println("DeltaY: " + deltaY);
    }
    
}
