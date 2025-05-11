package Controller;

import models.entities.User;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import models.dao.UserDAO;
import models.services.UserService;
import utils.AlertUtils;
import utils.SceneController;
import utils.SessionManager;


public class LoginScreenController {

    private final UserService userService;

    @FXML
    private TextField username;

    @FXML
    private TextField password;

    /**
     * Constructor khởi tạo service với DAO tương ứng.
     */
    public LoginScreenController() {
        this.userService = new UserService(new UserDAO());
    }

    /**
     * Xử lý sự kiện khi người dùng nhấn nút "Đăng nhập".
     * Kiểm tra hợp lệ đầu vào, xác thực thông tin, và chuyển đến giao diện chính nếu thành công.
     * Nếu không thành công, hiển thị cảnh báo tương ứng.
     */
    public void login() {
        String usn = username.getText();
        String pass = password.getText();

        if (usn.isEmpty() || pass.isEmpty()) {
            AlertUtils.showAlert("Lỗi", "vui lòng nhập thông tin!", Alert.AlertType.ERROR);
            return;
        }
        try {
            User user = userService.login(usn, pass);
            if (user != null) {
                SessionManager.setCurrentUser(user);
                SceneController.getInstance().initRootLayout("/fxml/BaseLayout.fxml");
            } else {
                AlertUtils.showAlert("Lỗi", "Tên tài khoản hoặc mật khẩu của quý khách không chính xác!", Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            AlertUtils.showAlert("Lỗi hệ thống", "Đã xảy ra lỗi khi đăng nhập!", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    /**
     * Xử lý khi người dùng nhấn nút "Đăng ký".
     * Chuyển sang giao diện đăng ký tài khoản.
     */
    public void signUp() {
        SceneController.getInstance().switchToScene("/fxml/SignUpScene.fxml");
    }
}
