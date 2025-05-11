package Controller;

import Controller.ProfileScreenController;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.*;
import models.dao.UserDAO;
import models.entities.User;
import models.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import utils.AlertUtils;
import utils.SceneController;
import utils.SessionManager;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ProfileScreenControllerTest {

    private ProfileScreenController controller;

    private TextField usernameField;
    private TextField emailField;
    private TextField phoneField;
    private Label roleLabel;
    private Label nameLabel;
    private Label truyendangmuon;
    private Label truyendatra;
    private Button editButton;

    @BeforeEach
    public void setUp() {
        new JFXPanel(); // Khởi động JavaFX toolkit

        controller = new ProfileScreenController();

        usernameField = new TextField();
        emailField = new TextField();
        phoneField = new TextField();
        roleLabel = new Label();
        nameLabel = new Label();
        truyendangmuon = new Label();
        truyendatra = new Label();
        editButton = new Button();

        TestUtils.setPrivateField(controller, "usernameField", usernameField);
        TestUtils.setPrivateField(controller, "emailField", emailField);
        TestUtils.setPrivateField(controller, "phoneField", phoneField);
        TestUtils.setPrivateField(controller, "roleLabel", roleLabel);
        TestUtils.setPrivateField(controller, "nameLabel", nameLabel);
        TestUtils.setPrivateField(controller, "Truyendangmuon", truyendangmuon);
        TestUtils.setPrivateField(controller, "Truyendatra", truyendatra);
        TestUtils.setPrivateField(controller, "Editbutton", editButton);
    }

    @Test
    public void testExit_callsSceneSwitch() {
        try (MockedStatic<SceneController> sceneController = Mockito.mockStatic(SceneController.class)) {
            SceneController mock = mock(SceneController.class);
            sceneController.when(SceneController::getInstance).thenReturn(mock);

            controller.Exit();

            sceneController.verify(() ->
                    mock.switchCenterContent("/fxml/HomePageScene.fxml"));
        }
    }

    @Test
    public void testEditProfile_toggleEditingMode() {
        controller.EditProfile(); // Bật chế độ chỉnh sửa
        assertFalse(usernameField.isDisabled());
        assertEquals("Lưu thay đổi", editButton.getText());

        controller.EditProfile(); // Lưu thay đổi
        assertEquals("Chỉnh sửa", editButton.getText());
    }

    @Test
    public void testSaveNewInfo_success() {
        User mockUser = new User(1, "oldUser", "old@email.com", "0123", "user");
        try (
                MockedStatic<SessionManager> sessionMock = Mockito.mockStatic(SessionManager.class);
                MockedStatic<AlertUtils> alertMock = Mockito.mockStatic(AlertUtils.class)
        ) {
            sessionMock.when(SessionManager::getCurrentUser).thenReturn(mockUser);

            usernameField.setText("newUser");
            emailField.setText("new@email.com");
            phoneField.setText("0999");

            UserService userService = mock(UserService.class);
            when(userService.editProfile(1, "newUser", "new@email.com", "0999")).thenReturn(true);

            // Inject mock userService
            TestUtils.setPrivateField(controller, "usernameField", usernameField);
            TestUtils.setPrivateField(controller, "emailField", emailField);
            TestUtils.setPrivateField(controller, "phoneField", phoneField);

            controller.saveNewInfo();

            sessionMock.verify(() -> SessionManager.setCurrentUser(any(User.class)));
            alertMock.verify(() ->
                    AlertUtils.showAlert("Thành Công", "Chỉnh sửa thông tin thành công", Alert.AlertType.INFORMATION)
            );
        }
    }

    @Test
    public void testSaveNewInfo_failure() {
        User mockUser = new User(1, "oldUser", "old@email.com", "0123", "user");
        try (
                MockedStatic<SessionManager> sessionMock = Mockito.mockStatic(SessionManager.class);
                MockedStatic<AlertUtils> alertMock = Mockito.mockStatic(AlertUtils.class)
        ) {
            sessionMock.when(SessionManager::getCurrentUser).thenReturn(mockUser);

            usernameField.setText("fail");
            emailField.setText("fail");
            phoneField.setText("fail");

            UserService spyService = spy(new UserService(new UserDAO()));
            doReturn(false).when(spyService).editProfile(1, "fail", "fail", "fail");

            controller.saveNewInfo();

            alertMock.verify(() ->
                    AlertUtils.showAlert("Lỗi", "Thông tin không hợp lệ", Alert.AlertType.ERROR)
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
}
