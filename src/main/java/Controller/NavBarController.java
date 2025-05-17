package Controller;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import models.entities.User;
import utils.SceneController;
import utils.SessionManager;

import java.io.IOException;

public class NavBarController {

    @FXML
    private Label adminFunctionText;

    @FXML
    private Button darkModeButton;

    private boolean isDarkMode = false;

    /**
     * Khởi tạo controller. Thiết lập listener cho nút dark mode và ẩn các thành phần UI tùy theo vai trò người dùng.
     */
    public void initialize() {
        darkModeButton.setOnAction(e -> toggleTheme());
        updateUIByRole();
    }

    /**
     * Chuyển đổi giao diện giữa light mode và dark mode bằng cách thay đổi stylesheet.
     */
    private void toggleTheme() {
        Scene scene = darkModeButton.getScene();
        if (scene != null) {
            ObservableList<String> stylesheets = scene.getStylesheets();
            stylesheets.clear(); // Xoá CSS cũ

            if (isDarkMode) {
                stylesheets.add(getClass().getResource("/css/styles.css").toExternalForm());
                darkModeButton.setText("Dark Mode");
            } else {
                stylesheets.add(getClass().getResource("/css/dark.css").toExternalForm());
                darkModeButton.setText("Light Mode");
            }

            isDarkMode = !isDarkMode;
        }
    }

    /** Chuyển đến trang chủ */
    public void home() {
        SceneController.getInstance().switchCenterContent("/fxml/HomePageScene.fxml");
    }

    /** Chuyển đến trang quản trị (chỉ dành cho admin) */
    public void ToAdminScene() {
        SceneController.getInstance().switchCenterContent("/fxml/AdminScene.fxml");
    }

    /** Chuyển đến trang theo dõi */
    public void follow() {
        SceneController.getInstance().switchCenterContent("/fxml/FollowScene.fxml");
    }

    /** Chuyển đến trang lịch sử mượn/trả sách */
    public void history() {
        SceneController.getInstance().switchCenterContent("/fxml/HistoryScene.fxml");
    }

    /** Chuyển đến trang thông tin cá nhân */
    public void profile() {
        SceneController.getInstance().switchCenterContent("/fxml/ProfileScene.fxml");
    }

    /** Chuyển đến giao diện duyệt sách */
    public void searching() {
        SceneController.getInstance().switchCenterContent("/fxml/browseScreen.fxml.fxml");
    }

    /** Đăng xuất khỏi phiên làm việc và trở về màn hình đăng nhập */
    public void logOut() {
        SceneController.getInstance().switchToScene("/fxml/loginScreen.fxml");
        SessionManager.clearSession();
    }

    /**
     * Cập nhật giao diện theo vai trò của người dùng hiện tại.
     * Ẩn phần chức năng admin nếu người dùng không phải admin.
     */
    private void updateUIByRole() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser != null && !"admin".equalsIgnoreCase(currentUser.getRole())) {
            adminFunctionText.setVisible(false);
        }
    }
}
