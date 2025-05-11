package Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import models.dao.UserDAO;
import models.entities.Library;
import models.services.UserService;
import utils.AlertUtils;
import utils.SceneController;

public class SignUpScreenController {

    private final UserService userService;
    @FXML
    private TextField userName;
    @FXML
    private TextField password;
    @FXML
    private TextField email;
    @FXML
    private TextField phoneNumber;

    /**
     * Constructor khởi tạo dịch vụ người dùng với DAO tương ứng.
     */
    public SignUpScreenController() {
        this.userService = new UserService(new UserDAO());
    }

    /**
     * Xử lý hành động khi người dùng nhấn nút "Đăng ký".
     * Kiểm tra đầu vào, gọi service để đăng ký và điều hướng đến màn hình đăng nhập nếu thành công.
     * Nếu không hợp lệ hoặc đăng ký thất bại, hiển thị cảnh báo.
     */
    public void signUpFinal() {
        String usn = userName.getText();
        String pass = password.getText();
        String eml = email.getText();
        String phn = phoneNumber.getText();

        if (usn.isEmpty() || pass.isEmpty() || eml.isEmpty() || phn.isEmpty()) {
            AlertUtils.showAlert("Lỗi", "Vui lòng điền đầy đủ thông tin", Alert.AlertType.ERROR);
            return;
        }

        if (userService.signup(usn, pass, eml, phn)) {
           AlertUtils.showAlert("Thành công", "Đăng ký thành công!", Alert.AlertType.INFORMATION);
            SceneController.getInstance().switchToScene("/fxml/loginScreen.fxml");
        } else {
            AlertUtils.showAlert("Lỗi", "Đăng ký thất bại (username/email đã tồn tại hoặc thông tin không hợp lệ)", Alert.AlertType.ERROR);
        }
    }

    /**
     * Hủy đăng ký và quay lại màn hình đăng nhập.
     */
    public void cancelSignUp() {
        SceneController.getInstance().switchToScene("/fxml/loginScreen.fxml");
    }
}
