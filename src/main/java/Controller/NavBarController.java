package Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.entities.User;
import utils.SceneController;
import utils.SessionManager;

import java.io.IOException;

public class NavBarController {

    @FXML
    private Label adminFunctionText;

    public void initialize() {
        updateUIByRole();
    }
    public void home() {
        SceneController.getInstance().switchToScene("/HomePageScene.fxml");
    }

    public void follow() {
        SceneController.getInstance().switchToScene("/FollowScene.fxml");
    }

    public void history() {
        SceneController.getInstance().switchToScene("/HistoryScene.fxml");
    }

    public void profile() {
        SceneController.getInstance().switchToScene("/ProfileScene.fxml");
    }

    public void searching() {
        SceneController.getInstance().switchToScene("/searchingScreen.fxml");
    }

    private void updateUIByRole() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser != null && !"admin".equalsIgnoreCase(currentUser.getRole())) {
            adminFunctionText.setVisible(false);
        }
    }

    @FXML
    private void handleSearchButtonClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Search.fxml"));
            Parent root = loader.load();
            BookSearchController controller = loader.getController();
            controller.setUser(SessionManager.getCurrentUser());
            Stage searchStage = new Stage();
            searchStage.setScene(new Scene(root));
            searchStage.setTitle("Tìm kiếm sách");
            searchStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Lỗi khi mở trang tìm kiếm: " + e.getMessage());
        }
    }
}
