package Controller;

import utils.SceneController;

public class AdminSceneController {

    public void ToUser() {
        SceneController.getInstance().switchCenterContent("/fxml/librarianToUser.fxml");
    }

    public void ToEdit() {
        SceneController.getInstance().switchCenterContent("/fxml/LibrarianToEdit.fxml");
    }
}
