package Controller;

import javafx.scene.input.MouseEvent;
import utils.SceneController;

public class HistoryScreenController {

    public void returnToMenu(MouseEvent mouseEvent) {
        SceneController.getInstance().switchToScene("/fxml/HomePageScene.fxml");
    }
}