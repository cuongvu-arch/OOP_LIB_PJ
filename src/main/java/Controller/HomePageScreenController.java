package Controller;

import models.entities.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import utils.SceneController;
import utils.SessionManager;

import javafx.stage.Stage;
import java.io.IOException;

public class HomePageScreenController {
    @FXML
    private ChoiceBox<String> adminFunction;
    @FXML
    private Label adminFunctionText;

    public void initialize() {
    @FXML
    private Button searchButton;

    public void setUser(User user) {
        this.currentUser = user;
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

    @FXML
    private void handleSearchButtonClick(ActionEvent event) {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Search.fxml"));
            Parent root = loader.load();




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