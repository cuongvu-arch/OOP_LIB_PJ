package Controller;

import javafx.embed.swing.JFXPanel;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import models.dao.DocumentDAO;
import models.entities.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class HomePageScreenControllerTest {

    private HomePageScreenController controller;

    // Hàm tiện ích để set field private
    private void setPrivateField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    public void setUp() {
        // JavaFX toolkit khởi tạo
        new JFXPanel();

        controller = new HomePageScreenController();

        // Mock các FXML components
        GridPane booksGrid = new GridPane();
        Button prevButton = new Button();
        Button nextButton = new Button();
        Label pageLabel = new Label();

        setPrivateField(controller, "booksGrid", booksGrid);
        setPrivateField(controller, "prevButton", prevButton);
        setPrivateField(controller, "nextButton", nextButton);
        setPrivateField(controller, "pageLabel", pageLabel);
    }

    @Test
    public void testUpdatePaginationUI_FirstPage() {
        setPrivateField(controller, "currentPage", 1);

        controller.getClass().getDeclaredMethods();
        controller.getClass().getDeclaredMethods();

        controller.getClass().getDeclaredMethods();

        controller.getClass().getDeclaredMethods();
        invokeUpdatePaginationUI(controller, 6);

        Label label = (Label) getPrivateField(controller, "pageLabel");
        Button prevButton = (Button) getPrivateField(controller, "prevButton");
        Button nextButton = (Button) getPrivateField(controller, "nextButton");

        assertEquals("Trang 1", label.getText());
        assertTrue(prevButton.isDisable());
        assertFalse(nextButton.isDisable());
    }

    // Dùng reflection để gọi private method updatePaginationUI
    private void invokeUpdatePaginationUI(HomePageScreenController controller, int booksLoaded) {
        try {
            var method = HomePageScreenController.class.getDeclaredMethod("updatePaginationUI", int.class);
            method.setAccessible(true);
            method.invoke(controller, booksLoaded);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Lấy giá trị field private
    private Object getPrivateField(Object target, String fieldName) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Optional: Thêm test cho loadBooks nếu bạn mock được documentDAO
    @Test
    public void testLoadBooks_ShouldNotThrow() {
        // Giả lập documentDAO và inject
        DocumentDAO mockDAO = mock(DocumentDAO.class);
        List<Document> dummyBooks = Arrays.asList(new Document());
        try {
            when(mockDAO.getBooksPaginated(any(), anyInt(), anyInt())).thenReturn(dummyBooks);
        } catch (Exception e) {
            fail("Mock DAO bị lỗi: " + e.getMessage());
        }

        setPrivateField(controller, "documentDAO", mockDAO);
        setPrivateField(controller, "currentPage", 1);

        // Gọi initialize (nó sẽ gọi loadBooks bên trong)
        controller.initialize();

        // Không assert gì ở đây vì loadBooks chạy trong background thread,
        // bạn có thể assert sau nếu dùng TestFX hoặc mock thêm.
    }
}
