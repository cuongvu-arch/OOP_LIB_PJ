package Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import utils.SceneController;
import utils.SessionManager;

import java.io.IOException;

public class NavBarController {



    public void home() {
        SceneController.getInstance().switchToScene("/HomePageScreen.fxml");
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
