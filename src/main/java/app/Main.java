package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utils.SceneController;

import java.util.Objects;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        try {
            SceneController.getInstance().setPrimaryStage(stage);
            SceneController.getInstance().switchToScene("/loginScreen.fxml");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void main (String[] args) {
        launch(args);
    }

}
