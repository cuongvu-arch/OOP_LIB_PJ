package Controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import models.entities.User;
import utils.SceneController;
import utils.SessionManager;

public class ProfileScreenController {

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
}
