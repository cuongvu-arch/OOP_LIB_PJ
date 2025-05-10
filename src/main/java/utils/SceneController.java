package utils;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneController {
    private static SceneController instance;
    private Stage primaryStage;
    private BorderPane currentRootLayout; // Thêm biến để lưu root layout hiện tại

    private SceneController() {
    }

    public static SceneController getInstance() {
        if (instance == null) {
            instance = new SceneController();
        }
        return instance;
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    /**
     * Khởi tạo BaseLayout cho ứng dụng (chỉ cần gọi 1 lần khi bắt đầu)
     */
    public void initRootLayout(String baseLayoutPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(baseLayoutPath));
            this.currentRootLayout = loader.load();

            Scene scene = new Scene(currentRootLayout);

            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            primaryStage.setMaxWidth(1920);
            primaryStage.setMaxHeight(1080);
            primaryStage.setMinWidth(1000);
            primaryStage.setMinHeight(700);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Lỗi khởi tạo", "Không thể tải layout chính",
                    "Lỗi khi tải file: " + baseLayoutPath);
        }
    }

    /**
     * Chỉ thay đổi nội dung center khi đã có BaseLayout
     */
    public void switchCenterContent(String contentPath) {
        if (currentRootLayout == null) {
            showErrorAlert("Lỗi hệ thống", "Chưa khởi tạo BaseLayout",
                    "Vui lòng gọi initRootLayout() trước");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(contentPath));
            Parent centerContent = loader.load();
            currentRootLayout.setCenter(centerContent);
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Lỗi tải giao diện", "Không thể tải nội dung",
                    "Lỗi khi tải file: " + contentPath);
        }
    }

    /**
     * Dành cho các scene đặc biệt không dùng BaseLayout (như Login)
     */
    public void switchToScene(String fxmlPath) {
        try {
            // Tạo FXMLLoader và load FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            // Chỉnh sửa kích thước trước khi setScene
            primaryStage.setResizable(true); // Cho phép người dùng thay đổi kích thước cửa sổ

            // Thiết lập scene cho primaryStage
            primaryStage.setScene(scene);

            // Đảm bảo cửa sổ luôn ở giữa màn hình
            primaryStage.centerOnScreen();


            // Hiển thị cửa sổ
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Lỗi tải giao diện", "Không thể tải trang",
                    "Lỗi khi tải file: " + fxmlPath);
        }
    }


    private void showErrorAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}