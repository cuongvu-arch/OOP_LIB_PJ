package Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import models.dao.UserDAO;
import models.services.UserService;
import utils.SceneController;

public class SignUpScreenController {

    private final UserService userService;

    public SignUpScreenController() {
        this.userService = new UserService(new UserDAO());
    }

    @FXML private TextField userName;
    @FXML private TextField password;
    @FXML private TextField email;
    @FXML private TextField phoneNumber;

    public void signUpFinal() {
        String usn = userName.getText();
        String pass = password.getText();
        String eml = email.getText();
        String phn = phoneNumber.getText();

        if (usn.isEmpty() || pass.isEmpty() || eml.isEmpty() || phn.isEmpty()) {
            showAlert("Lỗi", "Vui lòng điền đầy đủ thông tin");
            return;
        }

        if (userService.signup(usn, pass, eml, phn)) {
            showAlert("Thành công", "Đăng ký thành công!");
            SceneController.getInstance().switchToScene("/fxml/loginScreen.fxml");
        } else {
            showAlert("Lỗi", "Đăng ký thất bại (username/email đã tồn tại hoặc thông tin không hợp lệ)");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
