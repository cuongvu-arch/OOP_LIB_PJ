package models.dao;

import models.entities.BorrowRecord;
import models.entities.BorrowedBookInfo;
import models.entities.Document;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BorrowRecordDAOTest {

    private static Connection conn;

    @BeforeAll
    static void setUp() throws SQLException {
        conn = DriverManager.getConnection("jdbc:h2:mem:lib_db;DB_CLOSE_DELAY=-1");

        Statement stmt = conn.createStatement();

        stmt.execute("""
            CREATE TABLE books (
                isbn VARCHAR(20) PRIMARY KEY,
                title VARCHAR(255),
                thumbnail_url VARCHAR(255)
            )
        """);

        stmt.execute("""
            CREATE TABLE borrow_records (
                borrow_id INT AUTO_INCREMENT PRIMARY KEY,
                user_id INT,
                isbn VARCHAR(20),
                borrow_date DATE,
                return_date DATE,
                status VARCHAR(50)
            )
        """);

        stmt.execute("INSERT INTO books VALUES ('1234567890', 'JUnit in Action', 'http://example.com/image.jpg')");
        stmt.close();
    }

    @AfterAll
    static void tearDown() throws SQLException {
        conn.close();
    }

    @Test
    void testAddAndIsBorrowed() throws SQLException {
        BorrowRecord record = new BorrowRecord(1, "1234567890", Date.valueOf("2025-05-01"), null, "BORROWED");
        BorrowRecordDAO.add(conn, record);

        boolean result = BorrowRecordDAO.isBorrowed(conn, 1, "1234567890");
        assertTrue(result);
    }

    @Test
    void testDelete() throws SQLException {
        BorrowRecord record = new BorrowRecord(2, "1234567890", Date.valueOf("2025-05-02"), null, "BORROWED");
        BorrowRecordDAO.add(conn, record);

        assertTrue(BorrowRecordDAO.isBorrowed(conn, 2, "1234567890"));

        BorrowRecordDAO.delete(conn, record);
        assertFalse(BorrowRecordDAO.isBorrowed(conn, 2, "1234567890"));
    }

    @Test
    void testMarkAsReturned() throws SQLException {
        BorrowRecord record = new BorrowRecord(3, "1234567890", Date.valueOf("2025-05-03"), null, "BORROWED");
        BorrowRecordDAO.add(conn, record);

        new BorrowRecordDAO().markAsReturned(conn, 3, "1234567890");

        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT return_date FROM borrow_records WHERE user_id = 3 AND isbn = '1234567890'")) {
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next());
            assertNotNull(rs.getDate("return_date"));
        }
    }

    @Test
    void testGetBorrowedBooksWithInfoByUserId() throws SQLException {
        BorrowRecord record = new BorrowRecord(4, "1234567890", Date.valueOf("2025-05-04"), null, "BORROWED");
        BorrowRecordDAO.add(conn, record);

        List<BorrowedBookInfo> list = new BorrowRecordDAO().getBorrowedBooksWithInfoByUserId(conn, 4);
        assertEquals(1, list.size());

        Document doc = list.get(0).getDocument();
        assertEquals("JUnit in Action", doc.getTitle());
    }
}
