package app;

import javafx.application.Application;
import javafx.stage.Stage;
import models.dao.UserDAO;
import models.data.DatabaseConnection;
import models.entities.Library;
import utils.SceneController;

import java.sql.Connection;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        try {
            Connection connection = DatabaseConnection.getConnection();
            UserDAO userDAO = new UserDAO();
            Library.setUserList(userDAO.getAllUser(connection));
            SceneController.getInstance().setPrimaryStage(stage);
            SceneController.getInstance().switchToScene("/fxml/loginScreen.fxml");
            stage.centerOnScreen();
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
