package Controller;

import app.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import utils.SceneController;


public class HomePageScreenController extends SceneController {
    @FXML
    private ChoiceBox<String> adminFunction;
    private User currentUser;
    @FXML
    private Label adminFunctionText;


    public void setUser(User user) {
        this.currentUser = user;
        updateUIByRole();
    }



    private void updateUIByRole() {
        if (!"admin".equalsIgnoreCase(currentUser.getRole())) {
            adminFunction.setVisible(false);
            adminFunctionText.setVisible(false);

        }
    }

    public void follow (MouseEvent event) {
        switchToFollowScreen(event);
    }

    public void switchToThuThu(ActionEvent event) {

    }
}
