package Controller;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import utils.SceneController;
import utils.SessionManager;

import java.io.IOException;

/**
 * Controller cho giao diện quản trị viên (admin), xử lý các sự kiện điều hướng giữa các màn hình quản lý.
 */
public class AdminSceneController {

    @FXML
    private Label toSearchLabel;
    /**
     * Label điều hướng đến giao diện người dùng.
     */
    @FXML
    private Label toUserLabel;

    /**
     * Label điều hướng đến giao diện chỉnh sửa thư viện.
     */
    @FXML
    private Label toLibraryLabel;

    /**
     * Khởi tạo controller sau khi các thành phần FXML đã được tải.
     * Gán các sự kiện click chuột cho các Label điều hướng.
     */
    @FXML
    void initialize() {
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

        //Gán sự kiện cho Label toSearchLabel
        toSearchLabel.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                ToAdd();
            }
        });
    }

    /**
     * Chuyển sang giao diện quản lý người dùng.
     * Giao diện này được định nghĩa trong file FXML `/fxml/LibrarianToUser.fxml`.
     */
    public void ToUser() {
        SceneController.getInstance().switchCenterContent("/fxml/LibrarianToUser.fxml");
    }

    /**
     * Chuyển sang giao diện chỉnh sửa nội dung thư viện.
     * Giao diện này được định nghĩa trong file FXML `/fxml/LibrarianToEdit.fxml`.
     */
    public void ToEdit() {
        SceneController.getInstance().switchCenterContent("/fxml/LibrarianToEdit.fxml");
    }

    public void ToAdd() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Search.fxml"));
            Parent root = loader.load();
            BookSearchController controller = loader.getController();
            controller.setUser(SessionManager.getCurrentUser());
            Stage searchStage = new Stage();
            searchStage.setScene(new Scene(root));
            searchStage.setTitle("Tìm kiếm sách");
            searchStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Lỗi khi mở trang tìm kiếm: " + e.getMessage());
        }
    }
}
