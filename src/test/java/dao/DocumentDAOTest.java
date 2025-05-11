package models.dao;

import models.entities.Document;
import models.entities.DocumentWithBorrowInfo;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DocumentDAOTest {

    private static Connection conn;

    @BeforeAll
    static void setUpDatabase() throws Exception {
        conn = DriverManager.getConnection("jdbc:h2:mem:testdb_" + System.currentTimeMillis() + ";DB_CLOSE_DELAY=-1");
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("""
            CREATE TABLE books (
                isbn VARCHAR(20) PRIMARY KEY,
                title VARCHAR(255),
                authors TEXT,
                publisher VARCHAR(255),
                publish_date VARCHAR(20),
                description TEXT,
                thumbnail_url TEXT,
                qr_code_path TEXT,
                google_books_url TEXT,
                total_quantity INT
            )
        """);

            stmt.execute("""
            CREATE TABLE borrow_records (
                id INT AUTO_INCREMENT PRIMARY KEY,
                isbn VARCHAR(20),
                return_date DATE
            )
        """);
        }
    }

    @AfterAll
    static void tearDownDatabase() throws Exception {
        if (conn != null) {
            conn.close();
        }
    }

    @BeforeEach
    void clearData() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM borrow_records");
            stmt.execute("DELETE FROM books");
        }
    }

    @Test
    void testAddAndGetBook() {
        Document doc = new Document(
                "isbn_add_1",
                "JUnit Testing Book",
                new String[]{"John Doe", "Jane Smith"},
                "Tech Publisher",
                "2020-01-01", // ✅ đúng định dạng
                "A book about testing.",
                "http://image.url",
                "path/to/qr"
        );
        doc.setGoogleBooksUrl("http://googlebooks.com/isbn_add_1");
        doc.setQuantity(5);

        boolean added = new DocumentDAO().addBook(doc);
        assertTrue(added);

        Document fetched = assertDoesNotThrow(() -> new DocumentDAO().getBookByIsbn("isbn_add_1"));
        assertNotNull(fetched);
        assertEquals("JUnit Testing Book", fetched.getTitle());
        assertEquals(2, fetched.getAuthors().length);
    }

    @Test
    void testUpdateBook() throws Exception {
        Document doc = new Document(
                "isbn_upd_1",
                "Old Title",
                new String[]{"Author A"},
                "Old Pub",
                "2010-01-01", // ✅ đúng định dạng
                "Old description.",
                "old_thumb.jpg",
                "old_qr.png"
        );
        doc.setQuantity(3);
        new DocumentDAO().addBook(doc);

        // Cập nhật nội dung
        doc.setTitle("New Title");
        doc.setDescription("Updated desc.");
        boolean updated = new DocumentDAO().updateBook(doc);
        assertTrue(updated);

        Document updatedDoc = new DocumentDAO().getBookByIsbn("isbn_upd_1");
        assertEquals("New Title", updatedDoc.getTitle());
        assertEquals("Updated desc.", updatedDoc.getDescription());
    }

    @Test
    void testDeleteBook() throws Exception {
        Document doc = new Document("isbn_del_1", "Delete Me", new String[]{}, "", "2022-01-01", "", "", "");
        doc.setQuantity(1);
        new DocumentDAO().addBook(doc);

        boolean deleted = new DocumentDAO().deleteBook("isbn_del_1");
        assertTrue(deleted);

        Document fetched = new DocumentDAO().getBookByIsbn("isbn_del_1");
        assertNull(fetched);
    }

    @Test
    void testGetQuantityByIsbnAndUpdate() throws Exception {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO books (isbn, title, authors, publish_date, total_quantity) VALUES ('isbn_qty_1', 'Title', '[]', '2020-01-01', 5)");
        }

        int qty = DocumentDAO.getQuantityByIsbn(conn, "isbn_qty_1");
        assertEquals(5, qty);

        DocumentDAO.updateBookQuantity(conn, "isbn_qty_1", -2);
        int newQty = DocumentDAO.getQuantityByIsbn(conn, "isbn_qty_1");
        assertEquals(3, newQty);
    }

    @Test
    void testGetAllDocumentsWithBorrowInfo() throws Exception {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO books (isbn, title, authors, publish_date, total_quantity) VALUES ('isbn_borrow_1', 'Book 1', '[]', '2021-01-01', 3)");
            stmt.execute("INSERT INTO books (isbn, title, authors, publish_date, total_quantity) VALUES ('isbn_borrow_2', 'Book 2', '[]', '2021-01-01', 2)");
            stmt.execute("INSERT INTO borrow_records (isbn, return_date) VALUES ('isbn_borrow_1', NULL)");
        }

        List<DocumentWithBorrowInfo> docs = DocumentDAO.getAllDocumentsWithBorrowInfo(conn);
        assertEquals(2, docs.size());

        DocumentWithBorrowInfo b1 = docs.stream().filter(d -> d.getIsbn().equals("isbn_borrow_1")).findFirst().orElse(null);
        assertNotNull(b1);
        assertEquals(3, b1.getTotalQuantity());
        assertEquals(1, b1.getCurrentlyBorrowed());
        assertEquals(2, b1.getAvailableQuantity());
    }
}
