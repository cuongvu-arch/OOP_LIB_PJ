package Controller;

import models.entities.User;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import utils.SceneController;
import utils.SessionManager;


public class HomePageScreenController {
    @FXML
    private ChoiceBox<String> adminFunction;
    @FXML
    private Label adminFunctionText;

    public void initialize() {
        updateUIByRole();
    }

    private void updateUIByRole() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser != null && !"admin".equalsIgnoreCase(currentUser.getRole())) {
            adminFunction.setVisible(false);
            adminFunctionText.setVisible(false);
        }
    }

    public void switchToThuThu() {
        SceneController.getInstance().switchToScene("/librarianToEdit.fxml");
    }

    public void switchToFollowScreen() {
        SceneController.getInstance().switchToScene("/followScreen.fxml");
    }

    public void follow() {
        SceneController.getInstance().switchToScene("/profileScreen.fxml");
    }
}
