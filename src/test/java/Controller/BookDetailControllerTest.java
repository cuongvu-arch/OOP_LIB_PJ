package Controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.control.ScrollPane;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.sql.Connection;

import Controller.BookDetailController;
import models.entities.Document;
import models.entities.User;
import models.dao.ReviewDAO;
import models.dao.BorrowRecordDAO;
import models.services.DocumentService;
import utils.SessionManager;
import models.data.DatabaseConnection;

@ExtendWith(MockitoExtension.class)
public class BookDetailControllerTest {

    private BookDetailController controller;

    @Mock
    private DocumentService mockDocumentService;

    @Mock
    private ReviewDAO reviewDAO;

    @BeforeEach
    void setup() throws Exception {
        controller = new BookDetailController();

        // Inject service và control
        setPrivateField(controller, "documentService", mockDocumentService);

        setPrivateField(controller, "ratingChoiceBox", new ChoiceBox<>());
        setPrivateField(controller, "regenerateQRButton", new Button());
        setPrivateField(controller, "qrCodeImageView", new ImageView());
        setPrivateField(controller, "bookCoverImageView", new ImageView());
        setPrivateField(controller, "bookTitleText", new Text());
        setPrivateField(controller, "bookAuthorsText", new Text());
        setPrivateField(controller, "publishDateText", new Text());
        setPrivateField(controller, "publisherText", new Text());
        setPrivateField(controller, "isbnText", new Text());
        setPrivateField(controller, "languageText", new Text());
        setPrivateField(controller, "avgRatingText", new Text());
        setPrivateField(controller, "descriptionTextArea", new TextArea());
        setPrivateField(controller, "closeButton", new Button());
        setPrivateField(controller, "commentsVBox", new VBox());
        setPrivateField(controller, "newCommentTextArea", new TextArea());
        setPrivateField(controller, "commentsScrollPane", new ScrollPane());
        setPrivateField(controller, "borrowButton", new Button());
    }

    @Test
    void testInitialize_AsAdmin_SetsUpUI() throws Exception {
        User admin = new User();
        admin.setRole("admin");
        SessionManager.setCurrentUser(admin);

        Platform.runLater(() -> controller.initialize());
        Thread.sleep(500);

        Button qrButton = (Button) getPrivateField(controller, "regenerateQRButton");
        assertFalse(qrButton.isDisable());
        assertTrue(qrButton.isVisible());
    }

    @Test
    void testHandleSubmitComment_WithValidComment_AddsReview() throws Exception {
        User user = new User(1, "test", "p", "e", "0", "user");
        SessionManager.setCurrentUser(user);

        Document doc = new Document();
        doc.setIsbn("123");
        setPrivateField(controller, "currentBook", doc);

        TextArea commentArea = new TextArea("Great!");
        setPrivateField(controller, "newCommentTextArea", commentArea);

        VBox commentBox = new VBox();
        setPrivateField(controller, "commentsVBox", commentBox);

        Platform.runLater(() -> {
            try {
                var method = controller.getClass().getDeclaredMethod("handleSubmitRating");
                method.setAccessible(true);
                method.invoke(controller);
            } catch (Exception e) {
                fail("Failed to invoke handleSubmitRating: " + e.getMessage());
            }
        });
        Thread.sleep(500);

        assertEquals("", commentArea.getText());
    }

    @Test
    void testHandleSubmitRating_WithValidRating_AddsRating() throws Exception {
        User user = new User(1, "test", "p", "e", "0", "user");
        SessionManager.setCurrentUser(user);

        Document doc = new Document();
        doc.setIsbn("456");
        setPrivateField(controller, "currentBook", doc);

        ChoiceBox<Integer> ratingBox = new ChoiceBox<>();
        ratingBox.getItems().addAll(1, 2, 3, 4, 5);
        ratingBox.setValue(4);
        setPrivateField(controller, "ratingChoiceBox", ratingBox);

        Platform.runLater(() -> {
            try {
                var method = controller.getClass().getDeclaredMethod("handleSubmitRating");
                method.setAccessible(true);
                method.invoke(controller);
            } catch (Exception e) {
                fail("Failed to invoke handleSubmitRating: " + e.getMessage());
            }
        });
        Thread.sleep(500);

        assertEquals(Integer.valueOf(4), ratingBox.getValue());
    }

    @Test
    void testHandleBorrowButtonClick_NotBorrowedYet_Success() throws Exception {
        User user = new User(2, "user", "p", "e", "0", "user");
        SessionManager.setCurrentUser(user);

        Document doc = new Document();
        doc.setIsbn("789");
        setPrivateField(controller, "currentBook", doc);

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            Connection fakeConn = mock(Connection.class);
            dbMock.when(DatabaseConnection::getConnection).thenReturn(fakeConn);

            try (MockedConstruction<BorrowRecordDAO> daoMock = mockConstruction(BorrowRecordDAO.class,
                    (mockDao, context) -> {
                        when(mockDao.isBorrowed(fakeConn, user.getId(), doc.getIsbn())).thenReturn(false);
                        doNothing().when(mockDao).add(eq(fakeConn), any());
                    })) {

                Platform.runLater(() -> {
                    try {
                        var method = controller.getClass().getDeclaredMethod("handleSubmitRating");
                        method.setAccessible(true);
                        method.invoke(controller);
                    } catch (Exception e) {
                        fail("Failed to invoke handleSubmitRating: " + e.getMessage());
                    }
                });
                Thread.sleep(500);
            }
        }
    }

    @Test
    void testSetBookData_WithBookData_SetsUI() throws Exception {
        Document doc = new Document();
        doc.setTitle("Book Title");
        doc.setIsbn("999");
        doc.setAuthors(new String[]{"Author A"});
        doc.setPublishedDate("2023");
        doc.setPublisher("Some Publisher");

        User admin = new User(10, "admin", "p", "e", "0", "admin");
        SessionManager.setCurrentUser(admin);

        when(mockDocumentService.regenerateQRCode(any(), eq(admin))).thenReturn("fake/path.png");

        Platform.runLater(() -> controller.setBookData(doc));
        Thread.sleep(500);

        Text titleText = (Text) getPrivateField(controller, "bookTitleText");
        assertEquals("Book Title", titleText.getText());
    }

    // ----- HÀM HỖ TRỢ REFLECTION -----
    private static void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static Object getPrivateField(Object target, String fieldName) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }
}
