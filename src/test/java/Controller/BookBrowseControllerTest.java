package Controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import models.entities.Document;
import models.services.DocumentService;
import utils.AlertUtils;
import utils.BookImageLoader;

@ExtendWith(MockitoExtension.class)
public class BookBrowseControllerTest {

    @Mock
    private DocumentService documentService;

    @Mock
    private FlowPane booksFlowPane;

    @InjectMocks
    private BookBrowseController controller;

    @BeforeAll
    static void initJavaFX() {
        // Khởi tạo JavaFX toolkit
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // JavaFX đã được khởi tạo, bỏ qua
        }
    }

    @BeforeEach
    void setUp() {
        // Khởi tạo các trường FXML
        controller.titleField = new javafx.scene.control.TextField();
        controller.authorField = new javafx.scene.control.TextField();
        controller.publishDateField = new javafx.scene.control.TextField();
        controller.searchButton = new javafx.scene.control.Button();
        controller.booksFlowPane = booksFlowPane;

        // Gọi initialize
        controller.initialize();
    }

    @Test
    void testHandleSearchButtonClick_NoCriteria() {
        // Thiết lập dữ liệu đầu vào
        controller.titleField.setText("");
        controller.authorField.setText("");
        controller.publishDateField.setText("");

        // Mock AlertUtils.showAlert
        try (var mockedStatic = mockStatic(AlertUtils.class)) {
            mockedStatic.when(() -> AlertUtils.showAlert(anyString(), anyString(), any(Alert.AlertType.class)))
                    .thenReturn(null);

            // Gọi phương thức
            controller.handleSearchButtonClick();

            // Kiểm tra kết quả
            mockedStatic.verify(() -> AlertUtils.showAlert(
                    eq("Thiếu thông tin"),
                    eq("Vui lòng nhập ít nhất một tiêu chí tìm kiếm."),
                    eq(Alert.AlertType.WARNING)
            ));
            verify(documentService, never()).searchBooks(anyString(), anyString(), anyString());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testHandleSearchButtonClick_SuccessfulSearch() throws SQLException, InterruptedException {
        // Thiết lập dữ liệu đầu vào
        controller.titleField.setText("Java Programming");
        controller.authorField.setText("John Doe");
        controller.publishDateField.setText("2020");

        // Mock dữ liệu trả về từ DocumentService
        Document doc = new Document(
                "1234567890", "Java Programming", new String[]{"John Doe"}, "Publisher", "2020-01-01",
                "A book about Java", "http://example.com/thumbnail.jpg");
        List<Document> searchResults = Arrays.asList(doc);
        when(documentService.searchBooks("Java Programming", "John Doe", "2020")).thenReturn(searchResults);

        // Sử dụng CountDownLatch để đợi task hoàn thành
        CountDownLatch latch = new CountDownLatch(1);

        // Gọi phương thức
        Platform.runLater(() -> controller.handleSearchButtonClick());

        // Đợi task hoàn thành
        assertTrue(latch.await(2, TimeUnit.SECONDS), "Search task did not complete in time");

        // Kiểm tra kết quả
        verify(booksFlowPane, atLeastOnce()).getChildren();
        verify(booksFlowPane, times(1)).setVisible(true);
    }

    @Test
    void testHandleSearchButtonClick_NoResults() throws SQLException, InterruptedException {
        // Thiết lập dữ liệu đầu vào
        controller.titleField.setText("Nonexistent Book");
        controller.authorField.setText("");
        controller.publishDateField.setText("");

        // Mock DocumentService trả về danh sách rỗng
        when(documentService.searchBooks("Nonexistent Book", "", "")).thenReturn(Arrays.asList());

        // Mock AlertUtils.showAlert
        try (var mockedStatic = mockStatic(AlertUtils.class)) {
            mockedStatic.when(() -> AlertUtils.showAlert(anyString(), anyString(), any(Alert.AlertType.class)))
                    .thenReturn(null);

            // Sử dụng CountDownLatch để đợi task hoàn thành
            CountDownLatch latch = new CountDownLatch(1);

            // Mock hành vi của FlowPane
            doAnswer(invocation -> {
                latch.countDown();
                return null;
            }).when(booksFlowPane).setVisible(false);

            // Gọi phương thức
            Platform.runLater(() -> controller.handleSearchButtonClick());

            // Đợi task hoàn thành
            assertTrue(latch.await(2, TimeUnit.SECONDS), "Search task did not complete in time");

            // Kiểm tra kết quả
            mockedStatic.verify(() -> AlertUtils.showAlert(
                    eq("Không tìm thấy"),
                    eq("Không tìm thấy sách phù hợp với tiêu chí tìm kiếm."),
                    eq(Alert.AlertType.INFORMATION)
            ));
            verify(booksFlowPane, times(1)).setVisible(false);
        }
    }

    @Test
    void testHandleSearchButtonClick_DatabaseError() throws SQLException, InterruptedException {
        // Thiết lập dữ liệu đầu vào
        controller.titleField.setText("Test Book");
        controller.authorField.setText("");
        controller.publishDateField.setText("");

        // Mock DocumentService ném ra SQLException
        when(documentService.searchBooks("Test Book", "", "")).thenThrow(new SQLException("Database error"));

        // Mock AlertUtils.showAlert
        try (var mockedStatic = mockStatic(AlertUtils.class)) {
            mockedStatic.when(() -> AlertUtils.showAlert(anyString(), anyString(), any(Alert.AlertType.class)))
                    .thenReturn(null);

            // Sử dụng CountDownLatch để đợi task hoàn thành
            CountDownLatch latch = new CountDownLatch(1);

            // Mock hành vi của searchButton
            controller.searchButton.setDisable(true);
            doAnswer(invocation -> {
                latch.countDown();
                return null;
            }).when(controller.searchButton).setDisable(false);

            // Gọi phương thức
            Platform.runLater(() -> controller.handleSearchButtonClick());

            // Đợi task hoàn thành
            assertTrue(latch.await(2, TimeUnit.SECONDS), "Search task did not complete in time");

            // Kiểm tra kết quả
            mockedStatic.verify(() -> AlertUtils.showAlert(
                    eq("Lỗi cơ sở dữ liệu"),
                    eq("Không thể truy vấn cơ sở dữ liệu: Database error"),
                    eq(Alert.AlertType.ERROR)
            ));
        }
    }

    @Test
    void testCreateBookCover_WithThumbnail() {
        // Tạo Document với thumbnail
        Document doc = new Document(
                "1234567890", "Test Book", new String[]{"Author"}, "Publisher", "2020-01-01",
                "Description", "http://example.com/thumbnail.jpg");

        // Mock BookImageLoader
        try (var mockedStatic = mockStatic(BookImageLoader.class)) {
            mockedStatic.when(() -> BookImageLoader.loadImage(anyString(), any(ImageView.class)))
                    .thenReturn(null);

            // Gọi phương thức
            ImageView coverView = controller.createBookCover(doc);

            // Kiểm tra kết quả
            assertNotNull(coverView);
            assertEquals(150, coverView.getFitWidth(), 0.001);
            assertEquals(200, coverView.getFitHeight(), 0.001);
            mockedStatic.verify(() -> BookImageLoader.loadImage(
                    eq("http://example.com/thumbnail.jpg"),
                    any(ImageView.class)
            ));
        }
    }

    @Test
    void testCreateBookCover_WithoutThumbnail() {
        // Tạo Document không có thumbnail
        Document doc = new Document(
                "1234567890", "Test Book", new String[]{"Author"}, "Publisher", "2020-01-01",
                "Description", null);

        // Gọi phương thức
        ImageView coverView = controller.createBookCover(doc);

        // Kiểm tra kết quả
        assertNotNull(coverView);
        assertEquals(150, coverView.getFitWidth(), 0.001);
        assertEquals(200, coverView.getFitHeight(), 0.001);
    }

    @Test
    void testResetUIState_ClearFields() {
        // Thiết lập trạng thái ban đầu
        controller.titleField.setText("Test");
        controller.authorField.setText("Author");
        controller.publishDateField.setText("2020");
        controller.currentDocument = new Document("1234567890", "Test Book");

        // Gọi phương thức với clearFields = true
        controller.resetUIState(true);

        // Kiểm tra kết quả
        assertNull(controller.currentDocument);
        assertEquals("", controller.titleField.getText());
        assertEquals("", controller.authorField.getText());
        assertEquals("", controller.publishDateField.getText());
    }

    @Test
    void testResetUIState_KeepFields() {
        // Thiết lập trạng thái ban đầu
        controller.titleField.setText("Test");
        controller.authorField.setText("Author");
        controller.publishDateField.setText("2020");
        controller.currentDocument = new Document("1234567890", "Test Book");

        // Gọi phương thức với clearFields = false
        controller.resetUIState(false);

        // Kiểm tra kết quả
        assertNull(controller.currentDocument);
        assertEquals("Test", controller.titleField.getText());
        assertEquals("Author", controller.authorField.getText());
        assertEquals("2020", controller.publishDateField.getText());
    }
}
