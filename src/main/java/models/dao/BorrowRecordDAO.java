package models.dao;

import models.entities.BorrowRecord;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BorrowRecordDAO {

    public static List<BorrowRecord> getAll(Connection conn) throws SQLException {
        List<BorrowRecord> records = new ArrayList<>();
        String sql = "SELECT user_id, isbn FROM borrow_records";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int userId = rs.getInt("user_id");
                String isbn = rs.getString("isbn");
                records.add(new BorrowRecord(userId, isbn));
            }
        }

        return records;
    }

    public static void add(Connection conn, BorrowRecord record) throws SQLException {
        String sql = "INSERT INTO borrow_records (user_id, isbn) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, record.getUserId());
            stmt.setString(2, record.getIsbn());
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

}
