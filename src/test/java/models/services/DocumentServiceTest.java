package models.services;

import models.dao.DocumentDAO;
import models.entities.Document;
import models.entities.User;
import models.services.DocumentService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.sql.SQLException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DocumentServiceTest {

    @InjectMocks
    private DocumentService documentService;

    @Mock
    private DocumentDAO mockDocumentDAO;

    private AutoCloseable closeable;

    private User adminUser;
    private User normalUser;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        adminUser = new User();
        adminUser.setRole("admin");
        normalUser = new User();
        normalUser.setRole("user");
    }

    @Test
    void testBookExists_returnsTrue() throws SQLException {
        when(mockDocumentDAO.bookExists("123")).thenReturn(true);
        assertTrue(documentService.bookExists("123"));
    }

    @Test
    void testDeleteBook_asAdmin_success() throws SQLException {
        when(mockDocumentDAO.bookExists("123")).thenReturn(true);
        when(mockDocumentDAO.deleteBook("123")).thenReturn(true);

        boolean result = documentService.deleteBook("123", adminUser);
        assertTrue(result);
    }

    @Test
    void testDeleteBook_asNormalUser_fails() throws SQLException {
        boolean result = documentService.deleteBook("123", normalUser);
        assertFalse(result);
        verify(mockDocumentDAO, never()).deleteBook(anyString());
    }

    @Test
    void testAddBook_duplicateISBN_returnsFalse() throws Exception {
        Document doc = new Document();
        doc.setIsbn("123");
        when(mockDocumentDAO.bookExists("123")).thenReturn(true);

        boolean result = documentService.addBook(doc, adminUser);
        assertFalse(result);
        verify(mockDocumentDAO, never()).addBook(any());
    }

    @Test
    void testUpdateBook_bookNotExist_returnsFalse() throws SQLException {
        Document doc = new Document();
        doc.setIsbn("123");
        when(mockDocumentDAO.bookExists("123")).thenReturn(false);

        boolean result = documentService.updateBook(doc, adminUser);
        assertFalse(result);
    }

    @Test
    void testUpdateBook_asNormalUser_returnsFalse() throws SQLException {
        Document doc = new Document();
        doc.setIsbn("123");
        boolean result = documentService.updateBook(doc, normalUser);
        assertFalse(result);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }
}

