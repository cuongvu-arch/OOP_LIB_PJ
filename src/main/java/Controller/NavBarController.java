package Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
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
        SceneController.getInstance().switchCenterContent("/fxml/HomePageScene.fxml");
    }

    public void ToAdminScene() {
        SceneController.getInstance().switchCenterContent("/fxml/AdminScene.fxml");
    }
    public void follow() {
        SceneController.getInstance().switchCenterContent("/fxml/FollowScene.fxml");
    }

    public void history() {
        SceneController.getInstance().switchCenterContent("/fxml/HistoryScene.fxml");
    }

    public void profile() {
        SceneController.getInstance().switchCenterContent("/fxml/ProfileScene.fxml");
    }

    public void searching() {
        SceneController.getInstance().switchCenterContent("/fxml/browseScreen.fxml.fxml");
    }

    public void logOut() {SceneController.getInstance().switchToScene("/fxml/loginScreen.fxml");
    SessionManager.clearSession();}

    private void updateUIByRole() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser != null && !"admin".equalsIgnoreCase(currentUser.getRole())) {
            adminFunctionText.setVisible(false);
        }
    }

    @FXML
    private void handleSearchButtonClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Search.fxml"));
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
