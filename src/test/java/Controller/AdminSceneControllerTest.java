package Controller;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.SceneController;

public class AdminSceneControllerTest {

    private AdminSceneController controller;
    private SceneController mockSceneController;

    @BeforeEach
    public void setUp() {
        controller = new AdminSceneController();

        // Mock SceneController singleton
        mockSceneController = mock(SceneController.class);
        setMockSceneControllerInstance(mockSceneController);
    }

    @Test
    public void testToUser() {
        controller.ToUser();
        verify(mockSceneController).switchCenterContent("/fxml/LibrarianToUser.fxml");
    }

    @Test
    public void testToEdit() {
        controller.ToEdit();
        verify(mockSceneController).switchCenterContent("/fxml/LibrarianToEdit.fxml");
    }

    // Hack singleton: dùng reflection để gán mock vào SceneController
    private void setMockSceneControllerInstance(SceneController mockInstance) {
        try {
            java.lang.reflect.Field instanceField = SceneController.class.getDeclaredField("instance");
            instanceField.setAccessible(true);
            instanceField.set(null, mockInstance);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set mock SceneController instance", e);
        }
    }
}
