package Controller;

import Controller.EditBookController;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import models.entities.Document;
import models.entities.User;
import models.services.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import utils.SessionManager;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EditBookControllerTest {

    private EditBookController controller;

    @BeforeEach
    public void setUp() {
        controller = new EditBookController();

        // Khởi tạo các thành phần giao diện bằng giả lập
        setPrivateField(controller, "titleTextField", new TextField("New Title"));
        setPrivateField(controller, "authorsTextField", new TextField("Author One, Author Two"));
        setPrivateField(controller, "publisherTextField", new TextField("Publisher"));
        setPrivateField(controller, "publishDateTextField", new TextField("2020-01-01"));
        setPrivateField(controller, "thumbnailUrlTextField", new TextField("https://example.com/image.jpg"));
        setPrivateField(controller, "descriptionTextArea", new TextArea("Sample description"));
        setPrivateField(controller, "bookCoverImageView", new ImageView());
        setPrivateField(controller, "cancelButton", new Button());

        // Mock DocumentService
        DocumentService mockService = mock(DocumentService.class);
        setPrivateField(controller, "documentService", mockService);
    }

    @Test
    public void testHandleSaveButtonClick_AdminSuccess() throws Exception {
        // Tạo sách đang chỉnh sửa
        Document oldDoc = new Document("1234567890", "Old Title", new String[]{"Old Author"}, "OldPub", "2019", "Old Desc", "");
        setPrivateField(controller, "currentBook", oldDoc);

        // Mock người dùng admin
        User adminUser = new User();
        adminUser.setRole("admin");
        SessionManager.setCurrentUser(adminUser);

        DocumentService mockService = getPrivateField(controller, "documentService");
        when(mockService.updateBook(any(), eq(adminUser))).thenReturn(true);

        // Gọi method
        callPrivateMethod(controller, "handleSaveButtonClick");

        // Kiểm tra giá trị mới được cập nhật
        Document updated = getPrivateField(controller, "currentBook");
        assertEquals("New Title", updated.getTitle());
        assertArrayEquals(new String[]{"Author One", "Author Two"}, updated.getAuthors());
    }

    @Test
    public void testHandleSaveButtonClick_NonAdminDenied() throws Exception {
        Document oldDoc = new Document("1234567890", "Old Title", new String[]{"Old Author"}, "OldPub", "2019", "Old Desc", "");
        setPrivateField(controller, "currentBook", oldDoc);

        // Không phải admin
        User normalUser = new User();
        normalUser.setRole("user");
        SessionManager.setCurrentUser(normalUser);

        callPrivateMethod(controller, "handleSaveButtonClick");

        // Không được update, dữ liệu cũ vẫn giữ nguyên
        Document result = getPrivateField(controller, "currentBook");
        assertEquals("Old Title", result.getTitle());
    }

    // ==== Tiện ích ====

    private void setPrivateField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T getPrivateField(Object target, String fieldName) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return (T) field.get(target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void callPrivateMethod(Object target, String methodName, Object... args) {
        try {
            Class<?>[] types = new Class[args.length];
            for (int i = 0; i < args.length; i++) {
                types[i] = args[i].getClass();
            }
            var method = target.getClass().getDeclaredMethod(methodName, types);
            method.setAccessible(true);
            method.invoke(target, args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

