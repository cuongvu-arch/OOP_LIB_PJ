package utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneController {

    private static SceneController instance;

    private Stage primaryStage;

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

    public void switchToScene(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            // Thiết lập stage
            primaryStage.setScene(scene);

            // Tự động điều chỉnh kích thước theo nội dung
            primaryStage.sizeToScene();

            // Căn giữa màn hình
            primaryStage.centerOnScreen();

            // Cho phép maximize nhưng không cố định kích thước
            primaryStage.setMaximized(true);

            // Thiết lập kích thước tối thiểu nếu cần
            primaryStage.setMinWidth(1000);
            primaryStage.setMinHeight(700);

            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            // Xử lý lỗi tốt hơn
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi tải giao diện");
            alert.setHeaderText("Không thể tải trang");
            alert.setContentText("Lỗi khi tải file: " + fxmlPath);
            alert.showAndWait();
        }
    }
}
