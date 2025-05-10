package models.dao;

import models.entities.BorrowRecord;
import models.entities.BorrowedBookInfo;
import models.entities.Document;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BorrowRecordDAO {

    public static List<BorrowRecord> getAll(Connection conn) throws SQLException {
        List<BorrowRecord> records = new ArrayList<>();
        String sql = "SELECT user_id, isbn, borrow_date, return_date FROM borrow_records";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int userId = rs.getInt("user_id");
                String isbn = rs.getString("isbn");
                Date borrowDate = rs.getDate("borrow_date");
                Date returnDate = rs.getDate("return_date");
                records.add(new BorrowRecord(userId, isbn, borrowDate, returnDate));
            }
        }

        return records;
    }

    public static void add(Connection conn, BorrowRecord record) throws SQLException {
        String sql = "INSERT INTO borrow_records (user_id, isbn, borrow_date) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, record.getUserId());
            stmt.setString(2, record.getIsbn());
            stmt.setDate(3, record.getBorrowDate());
            stmt.executeUpdate();
        }
    }

    public static void delete(Connection conn, BorrowRecord record) throws SQLException {
        String sql = "DELETE FROM borrow_records WHERE user_id = ? AND isbn = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, record.getUserId());
            stmt.setString(2, record.getIsbn());
            stmt.executeUpdate();
        }
    }

    public static boolean isBorrowed(Connection conn, int userId, String isbn) throws SQLException {
        String sql = "SELECT 1 FROM borrow_records WHERE user_id = ? AND isbn = ? LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, isbn);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next(); // nếu có kết quả -> đã mượn
            }
        }
    }

    public List<BorrowedBookInfo> getBorrowedBooksWithInfoByUserId(Connection conn, int userId) throws SQLException {
        List<BorrowedBookInfo> list = new ArrayList<>();
        String sql = """
        SELECT br.isbn, br.borrow_date, br.return_date,
               d.title, d.thumbnail_url
        FROM borrow_records br
        JOIN books d ON br.isbn = d.isbn
        WHERE br.user_id = ?
    """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String isbn = rs.getString("isbn");
                    Date borrowDate = rs.getDate("borrow_date");
                    Date returnDate = rs.getDate("return_date");
                    String title = rs.getString("title");
                    String thumbnailUrl = rs.getString("thumbnail_url");

                    BorrowRecord record = new BorrowRecord(userId, isbn, borrowDate, returnDate);
                    Document document = new Document(title, isbn, thumbnailUrl);

                    list.add(new BorrowedBookInfo(document, record));
                }
            }
        }

        return list;
    }



    public void markAsReturned(Connection conn, int userId, String isbn) throws SQLException {
        String update = "UPDATE borrow_records SET return_date = CURRENT_DATE WHERE user_id = ? AND isbn = ? AND return_date IS NULL";
        try (PreparedStatement stmt = conn.prepareStatement(update)) {
            stmt.setInt(1, userId);
            stmt.setString(2, isbn);
            stmt.executeUpdate();
        }
    }

}
