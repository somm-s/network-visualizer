package ch.cydcampus.hickup.view;

import java.sql.Time;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import ch.cydcampus.hickup.Controller;
import ch.cydcampus.hickup.model.DataModel;
import ch.cydcampus.hickup.model.Token;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart.Data;
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
import javafx.stage.Stage;

public class View {
    private String observedHosts = "192.168.200";
    private TextField filterTextField;
    private TextField observedHostsTextField;
    private Button applyFilterButton;
    private Controller controller;
    private TimelineCanvas canvas;
    private DataModel model;
    private int updateCounter = 0;

    private final ComboBox<String> hostToHostComboBox = new ComboBox<String>(FXCollections.observableArrayList(""));
    private final ComboBox<String> hostComboBox = new ComboBox<String>(FXCollections.observableArrayList(""));


    public void setController(Controller controller, Stage primaryStage, DataModel model) {
        this.controller = controller;
        this.model = model;
        initializeUI(primaryStage);
    }

    private void initializeUI(Stage primaryStage) {

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
        
        // create the scene
        Scene scene = new Scene(root, 2000, 600);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        
        // set the scene
        primaryStage.setScene(scene);
        
        MenuBar menuBar = createMenuBar(primaryStage);
        root.getChildren().addAll(menuBar, container);


        // Code to create and configure UI elements
        filterTextField = new TextField();
        observedHostsTextField = new TextField(observedHosts);
        applyFilterButton = new Button("Play/Pause");

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
                canvas.setHostToHostFilter("");
                canvas.setObservedHost(filter);
            }        
        });
        controlPane.getChildren().add(hostComboBox);

        // Combobox for hosts
        hostToHostComboBox.setPromptText("Host to Host Filter");
        hostToHostComboBox.setOnAction(event -> {
            String filter = hostToHostComboBox.getValue();
            if(filter != null) {
                canvas.setHostToHostFilter(filter);
            }
        });
        controlPane.getChildren().add(hostToHostComboBox);

    }

    private MenuBar createMenuBar(Stage primaryStage) {
                // Menu bar
        MenuBar menuBar = new MenuBar();
        Menu menu = new Menu("File");
        Menu menu2 = new Menu("View");
        MenuItem item1 = new MenuItem("Öffnen...");
        MenuItem item2 = new MenuItem("Speichern...");
        MenuItem item3 = new MenuItem("Beenden...");
        
        menu.getItems().addAll(item1, item2, item3);
        menuBar.getMenus().addAll(menu, menu2);
        menuBar.prefWidthProperty().bind(primaryStage.widthProperty());
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

    public void updateView() {

        // update the hosts every now and then
        if(updateCounter++ % 100 == 0) {
            updateHosts();
        }


        canvas.draw();
    }


}
