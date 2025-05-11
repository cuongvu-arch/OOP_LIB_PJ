package Controller;

import Controller.ProfileScreenBorrowController1;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import models.entities.Document;
import models.entities.User;
import models.entities.BorrowedBookInfo;
import models.viewmodel.BookBorrowedView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import utils.SceneController;
import utils.SessionManager;
import java.lang.reflect.Field;


import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class ProfileScreenBorrowController1Test {

    private ProfileScreenBorrowController1 controller;

    private TableView<BookBorrowedView> tableView;
    private TableColumn<BookBorrowedView, String> bookInfoColumn;
    private Label labelThongtin;
    private Label labelTruyendatra;
    private Label nameLabel;

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
    public void setup() {
        new JFXPanel(); // Bắt buộc để khởi tạo JavaFX Toolkit

        controller = new ProfileScreenBorrowController1();

        // Mock các @FXML
        tableView = new TableView<>();
        bookInfoColumn = new TableColumn<>();
        labelThongtin = new Label();
        labelTruyendatra = new Label();
        nameLabel = new Label();

        // Inject bằng reflection
        TestUtils.setPrivateField(controller, "borrowedBooksTable", tableView);
        TestUtils.setPrivateField(controller, "bookInfoColumn", bookInfoColumn);
        TestUtils.setPrivateField(controller, "Thongtinchung1", labelThongtin);
        TestUtils.setPrivateField(controller, "Truyendatra1", labelTruyendatra);
        TestUtils.setPrivateField(controller, "nameLabel1", nameLabel);
    }

    @Test
    public void testExit_switchesToHomePageScene() {
        try (MockedStatic<SceneController> mockedScene = Mockito.mockStatic(SceneController.class)) {
            SceneController mockController = mock(SceneController.class);
            mockedScene.when(SceneController::getInstance).thenReturn(mockController);

            controller.Exit();

            mockedScene.verify(() ->
                    mockController.switchCenterContent("/fxml/HomePageScene.fxml")
            );
        }
    }

    @Test
    public void testInitialize_setsNameLabel() throws Exception {
        User mockUser = new User();
        mockUser.setUsername("john");

        try (MockedStatic<SessionManager> mockedSession = Mockito.mockStatic(SessionManager.class);
             MockedStatic<SceneController> mockedScene = Mockito.mockStatic(SceneController.class)) {

            mockedSession.when(SessionManager::getCurrentUser).thenReturn(mockUser);
            mockedScene.when(SceneController::getInstance).thenReturn(mock(SceneController.class));

            controller.initialize();

            // Đợi một chút cho task chạy xong (do bất đồng bộ)
            Thread.sleep(500);

            assertEquals("john", nameLabel.getText());
        }
    }

    @Test
    public void testInitialize_clickLabel_switchScene() {
        try (MockedStatic<SceneController> mockedScene = Mockito.mockStatic(SceneController.class)) {
            SceneController mockController = mock(SceneController.class);
            mockedScene.when(SceneController::getInstance).thenReturn(mockController);

            controller.initialize();

            labelThongtin.fireEvent(new javafx.scene.input.MouseEvent(
                    javafx.scene.input.MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0,
                    javafx.scene.input.MouseButton.PRIMARY, 1,
                    true, true, true, true, true, true, true, true, true, true, null
            ));
            labelTruyendatra.fireEvent(new javafx.scene.input.MouseEvent(
                    javafx.scene.input.MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0,
                    javafx.scene.input.MouseButton.PRIMARY, 1,
                    true, true, true, true, true, true, true, true, true, true, null
            ));

            mockedScene.verify(() -> mockController.switchCenterContent("/fxml/ProfileScene.fxml"));
            mockedScene.verify(() -> mockController.switchCenterContent("/fxml/ProfileSceneBorrow2.fxml"));
        }
    }
}

