package Controller;

import Controller.BookSearchController;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import models.entities.Document;
import models.entities.User;
import models.services.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BookSearchControllerTest {

    private BookSearchController controller;
    private DocumentService documentService;
    private User mockUser;


    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private Object invokePrivateMethod(Object target, String methodName, Class<?>[] paramTypes, Object... params) throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        return method.invoke(target, params);
    }

    @Test
    public void testIsValidIsbn_Valid10() throws Exception {
        boolean result = (boolean) invokePrivateMethod(controller, "isValidIsbn", new Class[]{String.class}, "123456789X");
        assertTrue(result);
    }

    @Test
    public void testIsValidIsbn_Valid13() throws Exception {
        boolean result = (boolean) invokePrivateMethod(controller, "isValidIsbn", new Class[]{String.class}, "9783161484100");
        assertTrue(result);
    }

    @Test
    public void testIsValidIsbn_Invalid() throws Exception {
        boolean result = (boolean) invokePrivateMethod(controller, "isValidIsbn", new Class[]{String.class}, "abc123");
        assertFalse(result);
    }

    @Test
    public void testUpdateButtonVisibility_AdminRole() throws Exception {
        setPrivateField(controller, "currentUser", mockUser);

        // Gán các nút để kiểm tra
        Button addBtn = new Button();
        Button updateBtn = new Button();
        Button deleteBtn = new Button();
        setPrivateField(controller, "addBookButton", addBtn);
        setPrivateField(controller, "updateBookButton", updateBtn);
        setPrivateField(controller, "deleteBookButton", deleteBtn);

        invokePrivateMethod(controller, "updateButtonVisibility", new Class[]{});

        assertTrue(addBtn.isVisible());
        assertTrue(updateBtn.isVisible());
        assertTrue(deleteBtn.isVisible());
    }

    @Test
    public void testUpdateAdminButtonStates_AdminAndExists() throws Exception {
        Document mockDoc = new Document();
        setPrivateField(controller, "currentDocument", mockDoc);

        Button addBtn = new Button();
        Button updateBtn = new Button();
        Button deleteBtn = new Button();
        setPrivateField(controller, "addBookButton", addBtn);
        setPrivateField(controller, "updateBookButton", updateBtn);
        setPrivateField(controller, "deleteBookButton", deleteBtn);

        invokePrivateMethod(controller, "updateAdminButtonStates", new Class[]{boolean.class}, true);

        assertTrue(addBtn.isDisabled());
        assertFalse(updateBtn.isDisabled());
        assertFalse(deleteBtn.isDisabled());
    }

    @Test
    public void testUpdateAdminButtonStates_AdminAndNotExists() throws Exception {
        Document mockDoc = new Document();
        setPrivateField(controller, "currentDocument", mockDoc);

        Button addBtn = new Button();
        Button updateBtn = new Button();
        Button deleteBtn = new Button();
        setPrivateField(controller, "addBookButton", addBtn);
        setPrivateField(controller, "updateBookButton", updateBtn);
        setPrivateField(controller, "deleteBookButton", deleteBtn);

        invokePrivateMethod(controller, "updateAdminButtonStates", new Class[]{boolean.class}, false);

        assertFalse(addBtn.isDisabled());
        assertTrue(updateBtn.isDisabled());
        assertTrue(deleteBtn.isDisabled());
    }

}

