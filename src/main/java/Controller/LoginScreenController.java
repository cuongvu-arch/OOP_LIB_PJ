package Controller;

import models.entities.User;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import models.dao.UserDAO;
import models.services.UserService;
import utils.SceneController;
import utils.SessionManager;


public class LoginScreenController {

    private final UserService userService;

    @FXML
    private TextField username;

    @FXML
    private TextField password;


    public LoginScreenController() {
        this.userService = new UserService(new UserDAO());
    }

    public void login () {
        String usn = username.getText();
        String pass = password.getText();

        if (usn.isEmpty() || pass.isEmpty()) {
            showAlert("Lỗi", "vui lòng nhập thông tin!");
            return;
        }
        try {
            User user = userService.login(usn, pass);
            if (user != null) {
                SessionManager.setCurrentUser(user);
                SceneController.getInstance().switchToScene("/fxml/HomePageScene.fxml");
            } else {
                showAlert("Lỗi", "Tên tài khoản hoặc mật khẩu của quý khách không chính xác!");
            }
        } catch (Exception e) {
            showAlert("Lỗi hệ thống", "Đã xảy ra lỗi khi đăng nhập!");
            e.printStackTrace();
        }
    }

    public void signUp () {
        SceneController.getInstance().switchToScene("/fxml/SignUpScene.fxml");
    }

        public void showAlert(String title, String message) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        }
    }
