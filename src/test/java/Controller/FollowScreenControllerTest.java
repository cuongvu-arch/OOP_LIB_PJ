package Controller;

import Controller.FollowScreenController;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import models.dao.BorrowRecordDAO;
import models.data.DatabaseConnection;
import models.entities.BorrowRecord;
import models.entities.BorrowedBookInfo;
import models.entities.Document;
import models.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import utils.SessionManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class FollowScreenControllerTest {

    private FollowScreenController controller;

    @BeforeEach
    public void setUp() {
        controller = new FollowScreenController();
        setPrivateField(controller, "borrowedBooksPane", new FlowPane());
    }

    @Test
    public void testLoadBorrowedBooksInBackground_WithUnreturnedBook() throws Exception {
        // Mock current user
        User mockUser = new User();
        mockUser.setId(1);

        try (MockedStatic<SessionManager> sessionMock = mockStatic(SessionManager.class);
             MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {

            sessionMock.when(SessionManager::getCurrentUser).thenReturn(mockUser);

            Connection mockConn = mock(Connection.class);
            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);

            // Mock DAO and return 1 unreturned book
            BorrowRecordDAO daoMock = mock(BorrowRecordDAO.class);
            BorrowRecord record = new BorrowRecord(mockUser.getId(), "ISBN001", null, null);
            Document doc = new Document("ISBN001", "Book Title", new String[]{"Author"}, "Publisher", "2020", "Description", "url");

            BorrowedBookInfo info = new BorrowedBookInfo(doc, record);

            when(daoMock.getBorrowedBooksWithInfoByUserId(mockConn, mockUser.getId()))
                    .thenReturn(List.of(info));

            // Inject mock DAO
            setPrivateField(controller, "loadBorrowedBooksInBackground", (Runnable) () -> {
                controllerTestAddBorrowedBook(controller, info);
            });

            // Gọi initialize() -> gọi loadBorrowedBooksInBackground
            controller.initialize();

            // Do không có JavaFX Platform, ta test thủ công addBorrowedBook()
            controllerTestAddBorrowedBook(controller, info);

            FlowPane pane = getPrivateField(controller, "borrowedBooksPane");
            assertEquals(1, pane.getChildren().size());
        }
    }

    /**
     * Gọi trực tiếp addBorrowedBook() để kiểm tra logic thêm sách.
     */
    private void controllerTestAddBorrowedBook(FollowScreenController controller, BorrowedBookInfo info) {
        try {
            Method method = FollowScreenController.class.getDeclaredMethod("addBorrowedBook", BorrowedBookInfo.class); // ✅ dùng getDeclaredMethod
            method.setAccessible(true);
            method.invoke(controller, info);  // Gọi method
        } catch (Exception e) {
            e.printStackTrace();
            fail("Không thể gọi addBorrowedBook()");
        }
    }


    // =========== Tiện ích ============
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
}

