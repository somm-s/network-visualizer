package ch.cydcampus.hickup;

import ch.cydcampus.hickup.model.AbstractionModule;
import ch.cydcampus.hickup.model.CombinationRule;
import ch.cydcampus.hickup.model.DataModel;
import ch.cydcampus.hickup.model.DataSource;
import javafx.scene.chart.PieChart.Data;

public class ApplicationLogic {

    private static ApplicationLogic instance = null;
    private DataModel dataModel;
    private DataSource dataSource;
    private CombinationRule combinationRule;
    private AbstractionModule abstractionModule;

    private ApplicationLogic() {
        // Make constructor private to avoid instantiation
    }

    public static ApplicationLogic getInstance() {
        if (instance == null) {
            instance = new ApplicationLogic();
        }
        return instance;
    }
    



}
