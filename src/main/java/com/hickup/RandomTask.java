package com.hickup;

import java.util.ArrayList;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

public class RandomTask extends Task<Void> {

    private ReadOnlyObjectWrapper<ObservableList<Data>> partialResults =
            new ReadOnlyObjectWrapper<>(this, "partialResults",
                    FXCollections.observableArrayList(new ArrayList<Data>()));

    public final ObservableList<Data> getPartialResults() { 
        return partialResults.get(); 
    }


    public final ReadOnlyObjectProperty<ObservableList<Data>> partialResultsProperty() {
        return partialResults.getReadOnlyProperty();
    } 
    
    @Override protected Void call() throws Exception {
        updateMessage("Creating Data...");
        for (int i=0; i<100; i++) {
            if (isCancelled()) break;

            final Data r = new Data(Math.random() * 100, new java.sql.Timestamp(System.currentTimeMillis()));
            Platform.runLater(new Runnable() { @Override public void run() {
                    partialResults.get().add(r);
                }
            });
            updateProgress(i, 100);

            // Now block the thread for a short time, but be sure
            // to check the interrupted exception for cancellation!
            try {
                Thread.sleep(Math.round(1000 * Math.random()));
            } catch (InterruptedException interrupted) {
                if (isCancelled()) {
                    updateMessage("Cancelled");
                    break;
                }
            }

        }
        return null;
    }
}