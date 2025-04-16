package Controller;

import app.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import models.DatabaseConnection;
import models.DatabaseManagement.UserManagement;
import utils.SceneController;
import utils.SessionManager;


public class LoginScreenController extends SceneController {
    private final UserManagement userManagement;

    @FXML
    private TextField username;

    @FXML
    private TextField password;


    public LoginScreenController() {
        DatabaseConnection dbConnection = new DatabaseConnection();
        this.userManagement = new UserManagement(dbConnection);
    }

    public void login (ActionEvent event) {
        String usn = username.getText();
        String pass = password.getText();

        if (usn.isEmpty() || pass.isEmpty()) {
            showAlert("Lỗi", "vui lòng nhập thông tin!");
            return;
        }
        try {
            User user = userManagement.login(usn, pass);
            if (user != null) {
                SessionManager.setCurrentUser(user);
                switchToHomeScene(event);
            } else {
                showAlert("Lỗi", "Tên tài khoản hoặc mật khẩu của quý khách không chính xác!");
            }
        } catch (Exception e) {
            showAlert("Lỗi hệ thống", "Đã xảy ra lỗi khi đăng nhập!");
            e.printStackTrace();
        }
    }

    public void signUp (ActionEvent event) {
        switchToSignupScene(event);
    }

        public void showAlert(String title, String message) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        }
    }
