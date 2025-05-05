package Controller;

import utils.SceneController;

public class AdminSceneController {

    public void ToUser() {
        SceneController.getInstance().switchToScene("/fxml/librarianToUser.fxml");
    }

    public void ToEdit() {
        SceneController.getInstance().switchToScene("/fxml/LibrarianToEdit.fxml");
    }
}
