package Controller;

import java.lang.reflect.Field;
import Controller.LoginScreenController;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import models.entities.User;
import models.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import utils.AlertUtils;
import utils.SceneController;
import utils.SessionManager;

import static org.mockito.Mockito.*;

public class LoginScreenControllerTest {

    private LoginScreenController controller;

    private TextField usernameField;
    private TextField passwordField;

    @BeforeEach
    public void setUp() {
        new JFXPanel(); // Khởi tạo JavaFX Toolkit

        controller = new LoginScreenController();

        usernameField = new TextField();
        passwordField = new TextField();

        TestUtils.setPrivateField(controller, "username", usernameField);
        TestUtils.setPrivateField(controller, "password", passwordField);
    }

    @Test
    public void testLogin_emptyFields_showErrorAlert() {
        usernameField.setText("");
        passwordField.setText("");

        try (MockedStatic<AlertUtils> mockedAlert = Mockito.mockStatic(AlertUtils.class)) {
            controller.login();

            mockedAlert.verify(() ->
                    AlertUtils.showAlert("Lỗi", "vui lòng nhập thông tin!", Alert.AlertType.ERROR)
            );
        }
    }

    @Test
    public void testLogin_invalidCredentials_showErrorAlert() {
        usernameField.setText("user");
        passwordField.setText("wrongpass");

        try (MockedStatic<AlertUtils> alertMock = Mockito.mockStatic(AlertUtils.class);
             MockedStatic<SessionManager> sessionMock = Mockito.mockStatic(SessionManager.class);
             MockedStatic<SceneController> sceneMock = Mockito.mockStatic(SceneController.class)) {

            // Mock userService bên trong controller
            UserService mockService = mock(UserService.class);
            when(mockService.login("user", "wrongpass")).thenReturn(null);

            // inject service bằng reflection
            TestUtils.setPrivateField(controller, "userService", mockService);

            controller.login();

            alertMock.verify(() ->
                    AlertUtils.showAlert("Lỗi", "Tên tài khoản hoặc mật khẩu của quý khách không chính xác!", Alert.AlertType.ERROR)
            );
        }
    }
    public class TestUtils {
        public static void setPrivateField(Object target, String fieldName, Object value) {
            try {
                Field field = target.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(target, value);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Test
    public void testLogin_validCredentials_switchScene() throws Exception {
        usernameField.setText("user");
        passwordField.setText("123");

        User fakeUser = new User();
        fakeUser.setUsername("user");

        try (MockedStatic<AlertUtils> alertMock = Mockito.mockStatic(AlertUtils.class);
             MockedStatic<SessionManager> sessionMock = Mockito.mockStatic(SessionManager.class);
             MockedStatic<SceneController> sceneMock = Mockito.mockStatic(SceneController.class)) {

            UserService mockService = mock(UserService.class);
            when(mockService.login("user", "123")).thenReturn(fakeUser);
            TestUtils.setPrivateField(controller, "userService", mockService);

            SceneController sceneController = mock(SceneController.class);
            sceneMock.when(SceneController::getInstance).thenReturn(sceneController);

            controller.login();

            sessionMock.verify(() -> SessionManager.setCurrentUser(fakeUser));
            sceneMock.verify(() -> sceneController.initRootLayout("/fxml/BaseLayout.fxml"));
            alertMock.verifyNoInteractions(); // Không hiển thị cảnh báo
        }
    }

    @Test
    public void testSignUp_switchesScene() {
        try (MockedStatic<SceneController> sceneMock = Mockito.mockStatic(SceneController.class)) {
            SceneController mockSceneController = mock(SceneController.class);
            sceneMock.when(SceneController::getInstance).thenReturn(mockSceneController);

            controller.signUp();

            sceneMock.verify(() -> mockSceneController.switchToScene("/fxml/SignUpScene.fxml"));
        }
    }
}
