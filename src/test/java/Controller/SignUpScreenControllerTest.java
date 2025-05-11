package Controller;

import Controller.SignUpScreenController;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import models.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import utils.AlertUtils;
import utils.SceneController;

import java.lang.reflect.Field;

import static org.mockito.Mockito.*;

public class SignUpScreenControllerTest {

    private SignUpScreenController controller;
    private TextField usernameField;
    private TextField passwordField;
    private TextField emailField;
    private TextField phoneField;

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

    @BeforeEach
    public void setUp() {
        new JFXPanel(); // Khởi động JavaFX toolkit

        controller = new SignUpScreenController();

        usernameField = new TextField();
        passwordField = new TextField();
        emailField = new TextField();
        phoneField = new TextField();

        TestUtils.setPrivateField(controller, "userName", usernameField);
        TestUtils.setPrivateField(controller, "password", passwordField);
        TestUtils.setPrivateField(controller, "email", emailField);
        TestUtils.setPrivateField(controller, "phoneNumber", phoneField);
    }

    @Test
    public void testSignUpFinal_withEmptyFields_showsError() {
        usernameField.setText("");
        passwordField.setText("");
        emailField.setText("");
        phoneField.setText("");

        try (MockedStatic<AlertUtils> alertMock = Mockito.mockStatic(AlertUtils.class)) {
            controller.signUpFinal();

            alertMock.verify(() ->
                    AlertUtils.showAlert("Lỗi", "Vui lòng điền đầy đủ thông tin", Alert.AlertType.ERROR)
            );
        }
    }

    @Test
    public void testSignUpFinal_success_showsSuccessAndSwitchScene() {
        usernameField.setText("newuser");
        passwordField.setText("pass");
        emailField.setText("test@email.com");
        phoneField.setText("0123456789");

        try (
                MockedStatic<AlertUtils> alertMock = Mockito.mockStatic(AlertUtils.class);
                MockedStatic<SceneController> sceneMock = Mockito.mockStatic(SceneController.class)
        ) {
            UserService mockService = mock(UserService.class);
            when(mockService.signup(anyString(), anyString(), anyString(), anyString())).thenReturn(true);
            TestUtils.setPrivateField(controller, "userService", mockService);

            SceneController mockScene = mock(SceneController.class);
            sceneMock.when(SceneController::getInstance).thenReturn(mockScene);

            controller.signUpFinal();

            alertMock.verify(() ->
                    AlertUtils.showAlert("Thành công", "Đăng ký thành công!", Alert.AlertType.INFORMATION)
            );
            sceneMock.verify(() ->
                    mockScene.switchToScene("/fxml/loginScreen.fxml")
            );
        }
    }

    @Test
    public void testSignUpFinal_failure_showsFailureMessage() {
        usernameField.setText("existuser");
        passwordField.setText("pass");
        emailField.setText("exist@email.com");
        phoneField.setText("0123456789");

        try (
                MockedStatic<AlertUtils> alertMock = Mockito.mockStatic(AlertUtils.class)
        ) {
            UserService mockService = mock(UserService.class);
            when(mockService.signup(anyString(), anyString(), anyString(), anyString())).thenReturn(false);
            TestUtils.setPrivateField(controller, "userService", mockService);

            controller.signUpFinal();

            alertMock.verify(() ->
                    AlertUtils.showAlert("Lỗi", "Đăng ký thất bại (username/email đã tồn tại hoặc thông tin không hợp lệ)", Alert.AlertType.ERROR)
            );
        }
    }

    @Test
    public void testCancelSignUp_switchesSceneToLogin() {
        try (MockedStatic<SceneController> sceneMock = Mockito.mockStatic(SceneController.class)) {
            SceneController mockScene = mock(SceneController.class);
            sceneMock.when(SceneController::getInstance).thenReturn(mockScene);

            controller.cancelSignUp();

            sceneMock.verify(() ->
                    mockScene.switchToScene("/fxml/loginScreen.fxml")
            );
        }
    }
}

