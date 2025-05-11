package Controller;

import Controller.TablePartController;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import models.dao.ReviewDAO;
import models.entities.Document;
import models.viewmodel.BookRatingView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TablePartControllerTest {

    private TablePartController controller;
    private TableView<BookRatingView> mockTableView;
    private TableColumn<BookRatingView, BookRatingView> mockColumn;

    @BeforeEach
    public void setUp() {
        new JFXPanel(); // Start JavaFX toolkit

        controller = new TablePartController();

        mockTableView = new TableView<>();
        mockColumn = new TableColumn<>();

        TestUtils.setPrivateField(controller, "documentTableView", mockTableView);
        TestUtils.setPrivateField(controller, "documentInfoColumn", mockColumn);
    }

    @Test
    public void testInitialize_shouldSetupTableAndLoadData() {
        // Giả lập dữ liệu trả về từ ReviewDAO
        Document doc1 = new Document();
        doc1.setTitle("Book One");
        doc1.setIsbn("ISBN001");

        Document doc2 = new Document();
        doc2.setTitle("Book Two");
        doc2.setIsbn("ISBN002");

        List<Document> mockDocs = List.of(doc1, doc2);

        try (MockedConstruction<ReviewDAO> mocked = mockConstruction(ReviewDAO.class, (mock, context) -> {
            when(mock.getTopRatedDocuments(10)).thenReturn(mockDocs);
        })) {
            controller.initialize();

            ObservableList<BookRatingView> items = mockTableView.getItems();
            assertEquals(2, items.size());
            assertEquals("Book One", items.get(0).titleProperty().get());
            assertEquals("ISBN001", items.get(0).isbnProperty().get());
            assertEquals("Book Two", items.get(1).titleProperty().get());
            assertEquals("ISBN002", items.get(1).isbnProperty().get());
        }
    }

    @Test
    public void testSetupDocumentTable_shouldSetupCellValueFactoryAndCellFactory() {
        controller.initialize();

        assertNotNull(mockColumn.getCellValueFactory());
        assertNotNull(mockColumn.getCellFactory());
    }

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
}

