package Controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import models.dao.UserDAO;
import models.entities.Library;
import models.entities.User;
import models.services.UserService;
import utils.SceneController;
import utils.SessionManager;

public class ProfileScreenController {

    private boolean isEditing = false;

    @FXML
    private Button Editbutton;

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;

    @FXML
    private Label roleLabel;

    @FXML
    private Label nameLabel;

    @FXML
    private Label Truyendangmuon;

    @FXML
    private Label Truyendatra;

    @FXML
    public void initialize() {
        Task<User> loadUserTask = new Task<>() {
            @Override
            protected User call() {
                return SessionManager.getCurrentUser();
            }
        };

        Truyendangmuon.setOnMouseClicked(event -> {
            // Đường dẫn tới file FXML hiển thị sách đang mượn
            SceneController.getInstance().switchCenterContent("/fxml/ProfileSceneBorrow1.fxml");
        });

        Truyendatra.setOnMouseClicked(event -> {
            // Đường dẫn tới file FXML hiển thị sách đang mượn
            SceneController.getInstance().switchCenterContent("/fxml/ProfileSceneBorrow2.fxml");
        });

        // Sau khi load xong, cập nhật lên UI thread
        loadUserTask.setOnSucceeded(event -> {
            User user = loadUserTask.getValue();
            if (user != null) {
                usernameField.setText(user.getUsername());
                usernameField.setDisable(true);
                nameLabel.setText(user.getUsername());
                emailField.setText(user.getEmail());
                emailField.setDisable(true);
                phoneField.setText(user.getPhoneNumber());
                phoneField.setDisable(true);
                roleLabel.setText(user.getRole());
            } else {
                showError("Không có thông tin người dùng.");
            }
        });

        new Thread(loadUserTask).start();
    }

    private void showError(String msg) {
        Platform.runLater(() -> {
            usernameField.setText("Lỗi");
            emailField.setText("Lỗi");
            phoneField.setText("Lỗi");
            roleLabel.setText("Lỗi: " + msg);
        });
    }

    public void Exit() {
        SceneController.getInstance().switchCenterContent("/fxml/HomePageScene.fxml");
    }

    public void EditProfile() {
        isEditing = !isEditing;
        if (isEditing) {
            usernameField.setDisable(false);
            emailField.setDisable(false);
            phoneField.setDisable(false);
            Editbutton.setText("Lưu thay đổi");
        } else {
            Editbutton.setText("Chỉnh sửa");
            saveNewInfo();

        }

    }

    public void saveNewInfo() {
        User currentUser = SessionManager.getCurrentUser();
        int currentUserId = currentUser.getId();
        String newUserName = usernameField.getText();
        String email = emailField.getText();
        String phoneNumber = phoneField.getText();
        UserService userService = new UserService(new UserDAO());
        if (userService.editProfile(currentUserId, newUserName, email, phoneNumber)) {
            currentUser.setUsername(newUserName);
            currentUser.setEmail(email);
            currentUser.setPhoneNumber(phoneNumber);
            SessionManager.setCurrentUser(currentUser);
            initialize();
            showAlert("Thành Công", "Chỉnh sửa thông tin thành công");
        } else {
            showAlert("Lỗi", "Thông tin không hợp lệ");
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
