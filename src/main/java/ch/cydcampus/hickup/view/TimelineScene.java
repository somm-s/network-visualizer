package ch.cydcampus.hickup.view;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import ch.cydcampus.hickup.Controller;
import ch.cydcampus.hickup.model.DataModel;
import ch.cydcampus.hickup.model.Token;
import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class TimelineScene extends Scene {
    private String observedHosts = "";
    private TextField filterTextField;
    private TextField observedHostsTextField;
    private Button applyFilterButton;
    private Controller controller;
    private TimelineCanvas canvas;
    private DataModel model;
    private int updateCounter = 0;

    private final ComboBox<String> hostToHostComboBox = new ComboBox<String>(FXCollections.observableArrayList(""));
    private final ComboBox<String> hostComboBox = new ComboBox<String>(FXCollections.observableArrayList(""));

    public TimelineScene(Parent parent, double width, double height, Controller controller, DataModel model) {
        super(parent, width, height);
        this.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        this.controller = controller;
        this.model = model;

        // Initialize canvas
        canvas = new TimelineCanvas(model);
        Pane canvasPane = new Pane(canvas);
        canvas.widthProperty().bind(canvasPane.widthProperty());
        canvas.heightProperty().bind(canvasPane.heightProperty());

        // create the root pane
        VBox root = new VBox();
        HBox container = new HBox();
        VBox.setVgrow(container, Priority.ALWAYS);
        VBox controlPane = new VBox();
        
        // initialize the control pane
        HBox.setHgrow(canvasPane, Priority.ALWAYS);
        HBox.setHgrow(controlPane, Priority.NEVER);
        container.getChildren().addAll(canvasPane, controlPane);
        
        MenuBar menuBar = createMenuBar();
        root.getChildren().addAll(menuBar, container);

        // Code to create and configure UI elements
        filterTextField = new TextField();
        observedHostsTextField = new TextField(observedHosts);
        applyFilterButton = new Button("Play/Pause");

        // Add listener to canvas for mouse movement for hover panel
        canvas.setOnMouseMoved(event -> {
            canvas.handleMouseMoved(event.getX(), event.getY());
        });

        // Set up event handlers
        applyFilterButton.setOnAction(event -> {
            canvas.togglePlayMode();
        });

        // handler for text field
        filterTextField.setOnAction(event -> {
            String filter = filterTextField.getText();
            canvas.setFilter(filter);
        });

        observedHostsTextField.setOnAction(event -> {
            String filter = observedHostsTextField.getText();
            System.out.println("Setting observed hosts to " + filter);
            observedHosts = filter;
            canvas.setObservedHost(filter);
        });

        canvas.setOnScroll(event -> {
            canvas.handleScroll(event.getDeltaY());
        });
        canvas.setOnMousePressed(event -> {
            canvas.handleMousePressed(event.getX());
        });

        canvas.setOnMouseDragged(event -> {
            canvas.handleMouseDragged(event.getX());
        });
        controlPane.getChildren().addAll(filterTextField, applyFilterButton, observedHostsTextField);

        // Combobox for host
        hostComboBox.setPromptText("Host Filter");
        hostComboBox.setOnAction(event -> {
            String filter = hostComboBox.getValue();
            if(filter != null) {
                if(filter.equals("")) {
                    canvas.setHostToHostFilter("");
                    canvas.setObservedHost("");
                    return;
                }

                String hostToHostFilter = hostToHostComboBox.getValue();
                
                // reset host to host filter if it does not contain the filter
                if(hostToHostFilter != null && !hostToHostFilter.contains(filter)) {
                    hostToHostComboBox.setValue(""); // TODO: Proper state machine for visualizer!
                }
                if(observedHosts.equals("")) {
                    observedHosts = filter;
                    observedHostsTextField.setText(observedHosts);
                }
                canvas.setObservedHostsPrefix(filter);
                canvas.setObservedHost(filter);

            }        
        });
        controlPane.getChildren().add(hostComboBox);

        // Combobox for hosts
        hostToHostComboBox.setPromptText("Host to Host Filter");
        hostToHostComboBox.setOnAction(event -> {
            String filter = hostToHostComboBox.getValue();
            if(filter != null) {
                if(filter.equals("")) {
                    canvas.setHostToHostFilter("");
                    return;
                }

                String firstHost = filter.split("-")[0];
                if(hostComboBox.getValue().equals("")) {
                    hostComboBox.setValue(firstHost);
                }
                canvas.setHostToHostFilter(filter);
            }
        });
        controlPane.getChildren().add(hostToHostComboBox);
        
        // button to exit timeline
        Button exitButton = new Button("Back to Menu");
        exitButton.setOnAction(event -> {
            controller.showMenuScene();
        });
        controlPane.getChildren().add(exitButton);


        // toggle port distinction mode button
        Button portDistinctionButton = new Button("Toggle Port Distinction");
        portDistinctionButton.setOnAction(event -> {
            canvas.togglePortDistinction();
        });
        controlPane.getChildren().add(portDistinctionButton);


        setRoot(root);
    }

    private MenuBar createMenuBar() {
                // Menu bar
        MenuBar menuBar = new MenuBar();
        Menu menu = new Menu("File");
        Menu menu2 = new Menu("View");
        MenuItem item1 = new MenuItem("Öffnen...");
        MenuItem item2 = new MenuItem("Speichern...");
        MenuItem item3 = new MenuItem("Beenden...");
        
        menu.getItems().addAll(item1, item2, item3);
        menuBar.getMenus().addAll(menu, menu2);
        menuBar.prefWidthProperty().bind(this.widthProperty());
        return menuBar;
    }

    private void updateHosts() {
        Set<String> hosts = new HashSet<String>(hostComboBox.getItems());
        Set<String> hostToHostOptions = new HashSet<String>();
        hostToHostOptions.add("");
        String observedHost = hostComboBox.getValue();
        String observedHostToHost = hostToHostComboBox.getValue();
        // get the list of hosts 
        Collection<Token> hostTokens = model.getRoot().getSubTokens();
        for(Token host : hostTokens) {
            String hostIdentifier = host.getState().getHostToHostIdentifier();
            String dst = host.getState().getDstIP();
            String src = host.getState().getSrcIP();
            if(src.startsWith(observedHosts)) {
                hosts.add(host.getState().getSrcIP());
            }

            if(dst.startsWith(observedHosts)) {
                hosts.add(host.getState().getDstIP());
            }

            if(observedHost == null) {
                hostToHostOptions.add(hostIdentifier);
            }

            if(observedHost != null && hostIdentifier.contains(observedHost)) {
                hostToHostOptions.add(hostIdentifier);
            }
        }
        hostComboBox.getItems().clear();
        hostComboBox.getItems().addAll(hosts);
        hostToHostComboBox.getItems().clear();
        hostToHostComboBox.getItems().addAll(hostToHostOptions);
        hostComboBox.setValue(observedHost);
        hostToHostComboBox.setValue(observedHostToHost);

    }

    public void setPlayingMode(boolean playingMode) {
        canvas.setPlayingMode(playingMode);
    }

    Token lastHoveredToken = null;

    public void updateTimelineView() {
        updateCounter++;
        if(updateCounter % 100 == 0) {
            updateHosts();
        }

        canvas.draw();

        // check hovered token
        Token hoveredToken = canvas.getHoveredToken();
        if(hoveredToken != null) {
            if(lastHoveredToken == null || !hoveredToken.equals(lastHoveredToken)) {
                System.out.println("Hovered token: " + hoveredToken);
                System.out.println("Hovered token state: " + hoveredToken.getState());
                lastHoveredToken = hoveredToken;
            }
        }
    }
}
