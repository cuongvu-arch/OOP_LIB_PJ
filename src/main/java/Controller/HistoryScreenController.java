package Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.input.MouseEvent;
import models.entities.User;
import utils.SceneController;
import utils.SessionManager;

public class HistoryScreenController {

    public void returnToMenu(MouseEvent mouseEvent) {
        SceneController.getInstance().switchToScene("/HomePageScene.fxml");
    }
}