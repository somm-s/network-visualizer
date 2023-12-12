package ch.cydcampus.hickup.view;

import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

/*
 * Displays information of a token when hovering over it
 */
public class HoverPanel extends VBox {

    public HoverPanel() {
    }

    public void setTokenInfo(String tokenInfo) {
        this.getChildren().clear();
        this.getChildren().add(new Text(tokenInfo));
    }
    
}
