package Controller;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import utils.SceneController;

public class AdminSceneController {

    @FXML
    private Label toUserLabel;

    @FXML
    private Label toLibraryLabel;

    @FXML
    private void initialize() {
        // Gán sự kiện cho Label toUserLabel
        toUserLabel.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                ToUser();
            }
        });

        // Gán sự kiện cho Label toLibraryLabel
        toLibraryLabel.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                ToEdit();
            }
        });
    }

    public void ToUser() {
        // Chuyển sang scene chứa nội dung cho người dùng
        SceneController.getInstance().switchCenterContent("/fxml/LibrarianToUser.fxml"); // Thay bằng đường dẫn đúng
    }

    public void ToEdit() {
        // Chuyển sang scene chứa nội dung thư viện
        SceneController.getInstance().switchCenterContent("/fxml/LibrarianToEdit.fxml"); // Thay bằng đường dẫn đúng
    }
}
