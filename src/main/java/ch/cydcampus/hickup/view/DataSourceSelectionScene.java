package ch.cydcampus.hickup.view;

import java.io.FileNotFoundException;
import java.util.Arrays;

import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapNativeException;

import ch.cydcampus.hickup.Controller;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;;


public class DataSourceSelectionScene extends Scene {

    private static final int DB_PARAMS = 10;
    private static final int FILE_PARAMS = 4;
    private static final int NETWORK_CAPTURE_PARAMS = 3;
    private static final int DOUBLE_INTERFACE_PARAMS = 4;
    private static final String[] DB_PARAM_NAMES = {"Host", "Port", "Database", "User", "Password", "Table", "Minimum Packet Size", "Host Selection", "Start Time", "End Time"};
    private static final String[] FILE_PARAM_NAMES = {"File Path", "Minimum Packet Size", "Start Time", "End Time"};
    private static final String[] NETWORK_CAPTURE_PARAM_NAMES = {"Interface", "Minimum Packet Size", "Berkley Packet Filter"};
    private static final String[] DOUBLE_INTERFACE_PARAM_NAMES = {"Interface 1", "Interface 2", "Minimum Packet Size", "Berkley Packet Filter"};
    private static final String[] DB_PARAM_PRESET = {"localhost", "5432", "ls22", "lab", "lab", "packets", "50", "94.246.227.141", "2022-04-18 00:00:00.000000", "2022-04-23 00:00:01.000000"};
    private static final String[] FILE_PARAM_PRESET = {"src/main/resources/test.ip.csv", "50", "", ""};
    private static final String[] NETWORK_CAPTURE_PARAM_PRESET = {"wlp0s20f3", "50", ""};
    private static final String[] DOUBLE_INTERFACE_PARAM_PRESET = {"wlp0s20f3", "wlp0s20f3", "50", ""};

    private Controller control;
    private StackPane rootPane;
    private VBox dataSourceSelectionView;
    private VBox configurationView;
    String selectedSource;



    public DataSourceSelectionScene(Parent root, double width, double height, Controller control) {
        super(root, width, height);
        this.control = control;
        this.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        // Initialize views
        dataSourceSelectionView = createDataSourceSelectionView();

        // Create root pane and add data source selection view initially
        rootPane = new StackPane(dataSourceSelectionView);
        setRoot(rootPane);
    }

    private VBox createDataSourceSelectionView() {
        Button button1 = createDataSourceButton("Network Capture", "interface.png");
        Button button2 = createDataSourceButton("File", "file.png");
        Button button3 = createDataSourceButton("Double Interface", "double_interface.png");
        Button button4 = createDataSourceButton("Database", "db.png");
    
        // Wrap each button in its own VBox
        VBox vbox1 = new VBox(button1);
        VBox vbox2 = new VBox(button2);
        VBox vbox3 = new VBox(button3);
        VBox vbox4 = new VBox(button4);
    
        HBox buttonContainer = new HBox(10);
        buttonContainer.getChildren().addAll(vbox1, vbox2, vbox3, vbox4);
    
        // Set VBox.VGrow for each button
        for (Button b : Arrays.asList(button1, button2, button3, button4)) {
            VBox.setVgrow(b, Priority.ALWAYS);
        }
    
        VBox.setMargin(buttonContainer, new Insets(20, 0, 20, 0)); // Top, Right, Bottom, Left
        VBox.setVgrow(buttonContainer, Priority.ALWAYS); // Set VBox to expand its children
    
        VBox dataSourceSelectionView = new VBox(20);
        dataSourceSelectionView.getChildren().addAll(buttonContainer);
    
        return dataSourceSelectionView;
    }

    private void populateUIElements(TextField[] textFields, Text[] labels, String[] paramNames, String[] paramPreset) {
        for(int i = 0; i < paramNames.length; i++) {
            textFields[i] = new TextField(paramPreset[i]);
            labels[i] = new Text(paramNames[i]);
        }
    }

    private VBox createConfigurationView() {
        
        // Create configuration components
        Text title = new Text();
        TextField[] parameterFields;
        Text[] parameterLabels;
        switch(selectedSource) {
            case "Network Capture":
                parameterFields = new TextField[NETWORK_CAPTURE_PARAMS];
                parameterLabels = new Text[NETWORK_CAPTURE_PARAMS];
                populateUIElements(parameterFields, parameterLabels, NETWORK_CAPTURE_PARAM_NAMES, NETWORK_CAPTURE_PARAM_PRESET);
                title.setText("Configure Network Capture");
                break;
            case "File":
                parameterFields = new TextField[FILE_PARAMS];
                parameterLabels = new Text[FILE_PARAMS];
                populateUIElements(parameterFields, parameterLabels, FILE_PARAM_NAMES, FILE_PARAM_PRESET);
                title.setText("Configure File");
                break;
            case "Double Interface":
                parameterFields = new TextField[DOUBLE_INTERFACE_PARAMS];
                parameterLabels = new Text[DOUBLE_INTERFACE_PARAMS];
                populateUIElements(parameterFields, parameterLabels, DOUBLE_INTERFACE_PARAM_NAMES, DOUBLE_INTERFACE_PARAM_PRESET);
                title.setText("Configure Double Interface");
                break;
            case "Database":
                parameterFields = new TextField[DB_PARAMS];
                parameterLabels = new Text[DB_PARAMS];
                populateUIElements(parameterFields, parameterLabels, DB_PARAM_NAMES, DB_PARAM_PRESET);
                title.setText("Configure Database");
                break;
            default:
                System.out.println("Error: Unknown data source description");
                return null;
        }

        // Create parameter text field for combination rule
        Text combinationRuleLabel = new Text("Combination Rule");
        TextField combinationRuleField = new TextField("Simple");

        // Create load button
        Button loadButton = new Button("Load");
        loadButton.setOnAction(event -> {
            // Retrieve parameters from text fields and pass them to the controller
            String[] params = new String[parameterFields.length];
            for(int i = 0; i < parameterFields.length; i++) {
                params[i] = parameterFields[i].getText();
            }

            // Switch back to data source selection view
            rootPane.getChildren().setAll(dataSourceSelectionView);
            
            try {
                control.loadDataSource(selectedSource, params, combinationRuleField.getText());
            } catch (FileNotFoundException | PcapNativeException | NotOpenException e) {
                System.out.println("Selected source " + selectedSource + " could not be loaded");
            }
        });

        // Create back button
        Button backButton = new Button("Back");
        backButton.setOnAction(event -> {
            // Switch back to data source selection view
            rootPane.getChildren().setAll(dataSourceSelectionView);
        });

        HBox buttonContainer = new HBox(10);
        buttonContainer.getChildren().addAll(loadButton, backButton);

        // Create configuration view layout
        VBox configurationView = new VBox(20);
        configurationView.setPadding(new Insets(20));
        configurationView.getChildren().addAll(title, combinationRuleLabel, combinationRuleField);
        for(int i = 0; i < parameterFields.length; i++) {
            configurationView.getChildren().addAll(parameterLabels[i], parameterFields[i]);
        }
        configurationView.getChildren().add(buttonContainer);

        return configurationView;
    }

    private Button createDataSourceButton(String description, String iconFileName) {
        // Create button
        Button button = new Button();

        // Create and set the icon for the button
        Image icon = new Image(getClass().getResourceAsStream("/" + iconFileName));
        ImageView iconView = new ImageView(icon);
        iconView.setFitWidth(50); // Set the width of the icon
        iconView.setFitHeight(50); // Set the height of the icon
        button.setGraphic(iconView);

        // Set the description as the button text
        button.setText(description);

        button.setOnAction(event -> {
            selectedSource = description;
            configurationView = createConfigurationView();
            ScrollPane scrollPane = new ScrollPane(configurationView);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);
            rootPane.getChildren().setAll(scrollPane);
        });

        return button;
    }

}
