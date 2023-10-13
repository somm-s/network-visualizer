package com.lockedshields;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;

public class UIButtonHandler implements EventHandler<ActionEvent> {

    Button retreiveButton;
    Button filterButton;

    DataBuffer dataBuffer;

    // text properties to get data from the textfields
    // start time property
    private final StringProperty startTime = new SimpleStringProperty("");
    private final StringProperty endTime = new SimpleStringProperty("");
    private final StringProperty observedHost = new SimpleStringProperty("");
    private final StringProperty dstHost = new SimpleStringProperty("");

    // Getter for srcHost property
    public String getObservedHost() {
        return observedHost.get();
    }

    // Setter for srcHost property
    public void setObservedHost(String value) {
        observedHost.set(value);
    }

    // Public method to get the StringProperty itself (useful for binding)
    public StringProperty observedHostProperty() {
        return observedHost;
    }

    // Getter for dstHost property
    public String getDstHost() {
        return dstHost.get();
    }

    // Setter for dstHost property
    public void setDstHost(String value) {
        dstHost.set(value);
    }

    // Public method to get the StringProperty itself (useful for binding)
    public StringProperty dstHostProperty() {
        return dstHost;
    }

    // Getter for endTime property
    public String getEndTime() {
        return endTime.get();
    }

    // Setter for endTime property
    public void setEndTime(String value) {
        endTime.set(value);
    }

    // Public method to get the StringProperty itself (useful for binding)
    public StringProperty endTimeProperty() {
        return endTime;
    }


    // Getter for startTime property
    public String getStartTime() {
        return startTime.get();
    }
    
    // Setter for startTime property
    public void setStartTime(String value) {
        startTime.set(value);
    }

    // Public method to get the StringProperty itself (useful for binding)
    public StringProperty startTimeProperty() {
        return startTime;
    }

    public UIButtonHandler(DataBuffer dataBuffer) {
        this.dataBuffer = dataBuffer;
    }

    public void setRetrieveButton(Button retreiveButton) {
        this.retreiveButton = retreiveButton;
    }

    public void setFilterButton(Button filterButton) {
        this.filterButton = filterButton;
    }

    @Override
    public void handle(ActionEvent event) {
        // get the data from the textfields
        String startTime = getStartTime();
        String endTime = getEndTime();
        String srcHost = getObservedHost();
        String dstHost = getDstHost();

        // check which button
        if (event.getSource() == retreiveButton) {
            System.out.println("Retreiving data");
            // use BackgroundLoaderTask to load data
            BackgroundLoaderTask task = new BackgroundLoaderTask(dataBuffer, startTime, endTime, srcHost, dstHost);
            Thread thread = new Thread(task);
            thread.start();
        } else if (event.getSource() == filterButton) {
            System.out.println("Filtering data");
            // filter the data
            dataBuffer.filterData(startTime, endTime, srcHost, dstHost);

        }

    }
    
}
