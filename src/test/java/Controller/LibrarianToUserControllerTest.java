package Controller;

import Controller.LibrarianToUserController;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import models.dao.BorrowRecordDAO;
import models.dao.UserDAO;
import models.data.DatabaseConnection;
import models.entities.BorrowRecord;
import models.entities.BorrowedBookInfo;
import models.entities.Document;
import models.entities.User;
import models.viewmodel.UserBorrowView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import java.lang.reflect.Field;


import java.sql.Connection;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class LibrarianToUserControllerTest {

    private LibrarianToUserController controller;

    @BeforeEach
    public void setUp() {
        // Khởi tạo JavaFX Toolkit (bắt buộc nếu chạy test JavaFX headless)
        new JFXPanel();

        controller = new LibrarianToUserController();

        // Tạo các thành phần UI giả lập
        TableView<UserBorrowView> mockTable = new TableView<>();
        TableColumn<UserBorrowView, String> usernameColumn = new TableColumn<>();
        TableColumn<UserBorrowView, String> borrowedColumn = new TableColumn<>();
        TableColumn<UserBorrowView, String> returnedColumn = new TableColumn<>();

        // Inject vào controller bằng reflection
        TestUtils.setPrivateField(controller, "tableView", mockTable);
        TestUtils.setPrivateField(controller, "usernameColumn", usernameColumn);
        TestUtils.setPrivateField(controller, "borrowedColumn", borrowedColumn);
        TestUtils.setPrivateField(controller, "returnedColumn", returnedColumn);
    }
    public class TestUtils {
        public static void setPrivateField(Object target, String fieldName, Object value) {
            try {
                Field field = target.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(target, value);
            } catch (Exception e) {
                throw new RuntimeException("Failed to set field: " + fieldName, e);
            }
        }
    }

    @Test
    public void testInitialize_loadsUserBorrowDataSuccessfully() throws Exception {
        // Tạo dữ liệu giả
        User fakeUser = new User();
        fakeUser.setId(1);
        fakeUser.setUsername("test_user");

        Document doc = new Document();
        doc.setTitle("Book A");

        BorrowRecord record = new BorrowRecord();
        record.setReturnDate(null); // Chưa trả

        BorrowedBookInfo info = new BorrowedBookInfo(doc, record);
        List<BorrowedBookInfo> borrowedList = List.of(info);

        // Mock static method getConnection() và getAllUser()
        try (MockedStatic<DatabaseConnection> dbMock = Mockito.mockStatic(DatabaseConnection.class);
             MockedStatic<UserDAO> userDaoMock = Mockito.mockStatic(UserDAO.class)) {

            Connection mockConn = mock(Connection.class);
            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);
            userDaoMock.when(() -> UserDAO.getAllUser(mockConn)).thenReturn(List.of(fakeUser));

            BorrowRecordDAO mockBorrowDAO = mock(BorrowRecordDAO.class);
            when(mockBorrowDAO.getBorrowedBooksWithInfoByUserId(mockConn, 1)).thenReturn(borrowedList);

            // Inject mock BorrowRecordDAO vào controller thông qua reflection nếu cần

            // Chạy hàm initialize
            controller.initialize();

            // Chờ Task chạy xong
            Thread.sleep(500);

            TableView<UserBorrowView> table = (TableView<UserBorrowView>) getPrivateField(controller, "tableView");
            ObservableList<UserBorrowView> items = table.getItems();

            assertEquals(1, items.size());
            UserBorrowView view = items.get(0);
            assertEquals("test_user", view.getUsername());
            assertEquals("Book A", view.getBorrowedBooks());
            assertEquals("chưa có cuốn sách nào", view.getReturnedBooks());
        }
    }

    private Object getPrivateField(Object target, String fieldName) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(target);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get field: " + fieldName, e);
        }
    }
}
