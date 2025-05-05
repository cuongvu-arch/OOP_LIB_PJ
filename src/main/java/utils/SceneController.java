package utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
            primaryStage.setScene(scene);
            primaryStage.setWidth(1920);  // Đặt chiều rộng
            primaryStage.setHeight(1080); // Đặt chiều cao
            primaryStage.setMaximized(true);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
