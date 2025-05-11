package Controller;

import Controller.HistoryScreenController;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import models.entities.BorrowRecord;
import models.entities.BorrowedBookInfo;
import models.entities.Document;
import models.entities.User;
import models.services.BorrowRecordService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import utils.SessionManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Date;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

public class HistoryScreenControllerTest {

    @BeforeAll
    public static void initToolkit() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.startup(latch::countDown);
        latch.await();
    }

    @AfterAll
    public static void tearDownToolkit() {
        Platform.exit();
    }

    @Test
    public void testLoadBorrowHistory() throws Exception {
        // Arrange
        HistoryScreenController controller = new HistoryScreenController();

        TableView<BorrowedBookInfo> table = new TableView<>();
        TableColumn<BorrowedBookInfo, String> col1 = new TableColumn<>();
        TableColumn<BorrowedBookInfo, String> col2 = new TableColumn<>();
        TableColumn<BorrowedBookInfo, String> col3 = new TableColumn<>();
        TableColumn<BorrowedBookInfo, String> col4 = new TableColumn<>();
        TableColumn<BorrowedBookInfo, String> col5 = new TableColumn<>();

        setPrivateField(controller, "historyTable", table);
        setPrivateField(controller, "bookNameColumn", col1);
        setPrivateField(controller, "borrowDateColumn", col2);
        setPrivateField(controller, "returnDateColumn", col3);
        setPrivateField(controller, "statusColumn", col4);
        setPrivateField(controller, "remainingDaysColumn", col5);

        Document doc = new Document();
        setPrivateField(doc, "title", "Sách A");

        BorrowRecord record = new BorrowRecord();
        setPrivateField(record, "isbn", "123");
        setPrivateField(record, "userId", 1);
        setPrivateField(record, "borrowDate", new Date(System.currentTimeMillis()));
        setPrivateField(record, "returnDate", null);

        BorrowedBookInfo info = new BorrowedBookInfo(doc, record);

        BorrowRecordService mockService = new BorrowRecordService() {
            @Override
            public java.util.List<BorrowedBookInfo> getBorrowedBooksByUserId(int userId) {
                return Collections.singletonList(info);
            }

            @Override
            public String getRemainingDays(BorrowRecord record) {
                return "5 ngày";
            }
        };

        setPrivateField(controller, "borrowService", mockService);

        // Act
        Method method = HistoryScreenController.class.getDeclaredMethod("loadBorrowHistory", int.class);
        method.setAccessible(true);
        Platform.runLater(() -> {
            try {
                method.invoke(controller, 1);
            } catch (Exception e) {
                e.printStackTrace();
                fail("Không thể gọi loadBorrowHistory()");
            }
        });

        Thread.sleep(1000); // đợi Task chạy xong

        // Assert
        ObservableList<BorrowedBookInfo> items = table.getItems();
        assertNotNull(items);
        assertEquals(1, items.size());
        assertEquals("Sách A", items.get(0).getDocument().getTitle());
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}

