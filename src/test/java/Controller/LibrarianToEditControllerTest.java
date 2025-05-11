package Controller;

import Controller.LibrarianToEditController;
import javafx.scene.control.Alert;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import models.entities.DocumentWithBorrowInfo;
import models.services.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LibrarianToEditControllerTest {

    private LibrarianToEditController controller;

    @BeforeEach
    public void setUp() throws Exception {
        controller = new LibrarianToEditController();

        setPrivateField(controller, "adjustIsbnField", createTextField("1234567890"));
        setPrivateField(controller, "adjustQuantityField", createTextField("5"));
        setPrivateField(controller, "bookTable", new TableView<DocumentWithBorrowInfo>());
    }

    private TextField createTextField(String text) {
        TextField textField = new TextField();
        textField.setText(text);
        return textField;
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    public void testHandleAdjustQuantity_Success() throws Exception {
        try (MockedStatic<DocumentService> mockedService = mockStatic(DocumentService.class)) {
            // Không throw exception → thành công
            mockedService.when(() -> DocumentService.adjustBookQuantity("1234567890", 5)).thenAnswer(inv -> null);

            // Gọi phương thức
            invokePrivateMethod(controller, "handleAdjustQuantity");

            mockedService.verify(() -> DocumentService.adjustBookQuantity("1234567890", 5), times(1));
        }
    }

    @Test
    public void testHandleAdjustQuantity_InvalidQuantity() throws Exception {
        setPrivateField(controller, "adjustQuantityField", createTextField("abc"));

        invokePrivateMethod(controller, "handleAdjustQuantity");
        // Dữ liệu không hợp lệ, không gọi đến DocumentService
    }

    @Test
    public void testHandleAdjustQuantity_EmptyISBN() throws Exception {
        setPrivateField(controller, "adjustIsbnField", createTextField(""));
        invokePrivateMethod(controller, "handleAdjustQuantity");
    }

    @Test
    public void testHandleAdjustQuantity_ZeroChange() throws Exception {
        setPrivateField(controller, "adjustQuantityField", createTextField("0"));
        invokePrivateMethod(controller, "handleAdjustQuantity");
    }

    @Test
    public void testHandleAdjustQuantity_IllegalArgument() throws Exception {
        try (MockedStatic<DocumentService> mockedService = mockStatic(DocumentService.class)) {
            mockedService.when(() -> DocumentService.adjustBookQuantity("1234567890", 5))
                    .thenThrow(new IllegalArgumentException("Không thể trừ quá số hiện có"));

            invokePrivateMethod(controller, "handleAdjustQuantity");
        }
    }

    private void invokePrivateMethod(Object instance, String methodName) throws Exception {
        Method method = instance.getClass().getDeclaredMethod(methodName);
        method.setAccessible(true);
        method.invoke(instance);
    }

}
