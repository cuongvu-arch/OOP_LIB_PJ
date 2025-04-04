package Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import models.DatabaseConnection;
import models.DatabaseManagement.UserManagement;
import utils.SceneController;

public class SignUpScreenController extends SceneController {

    private final UserManagement userManagement;

    public SignUpScreenController() {
        DatabaseConnection dbconnection = new DatabaseConnection();
        this.userManagement = new UserManagement(dbconnection);
    }


    @FXML
    private TextField userName;

    @FXML
    private TextField password;

    @FXML
    private TextField email;

    @FXML
    private TextField phoneNumber;

    public void signUpFinal(ActionEvent event) {
        try {
            String usn = userName.getText();
            String pass = password.getText();
            String eml = email.getText();
            String phn = phoneNumber.getText();

            if (usn.isEmpty() || pass.isEmpty() || eml.isEmpty() || phn.isEmpty()) {
                showAlert("Lỗi", "Vui lòng điền đầy đủ thông tin");
                return;
            }

            if (userManagement.signup(usn, pass, eml, phn)) {
                showAlert("Thành công", "Đăng ký thành công!");
                switchToLoginScene(event);
            } else {
                showAlert("Lỗi", "Đăng ký thất bại (username/email đã tồn tại hoặc thông tin không hợp lệ)");
            }
        } catch (Exception e) {
            showAlert("Lỗi hệ thống", "Đã xảy ra lỗi khi đăng ký!");
            e.printStackTrace();
        }
    }

    public void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
