package models.dao;

import models.data.DatabaseConnection;
import models.entities.BorrowRecord;
import models.entities.BorrowedBookInfo;
import models.entities.Document;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BorrowRecordDAO {

    public boolean isCurrentlyBorrowing(Connection conn, int userId, String isbn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM borrow_records WHERE user_id = ? AND isbn = ? AND return_date IS NULL";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, isbn);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    /**
     * Trả về danh sách tất cả bản ghi mượn sách trong cơ sở dữ liệu.
     *
     * @param conn Kết nối đến cơ sở dữ liệu.
     * @return Danh sách {@link BorrowRecord}.
     * @throws SQLException Nếu có lỗi SQL.
     */
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
                String status = rs.getString("status");
                records.add(new BorrowRecord(userId, isbn, borrowDate, returnDate, status));
            }
        }

        return records;
    }

    /**
     * Thêm một bản ghi mượn sách mới vào cơ sở dữ liệu.
     *
     * @param conn   Kết nối đến cơ sở dữ liệu.
     * @param record Đối tượng {@link BorrowRecord} cần thêm.
     * @throws SQLException Nếu có lỗi SQL.
     */
    public static void add(Connection conn, BorrowRecord record) throws SQLException {
        String sql = "INSERT INTO borrow_records (user_id, isbn, borrow_date) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, record.getUserId());
            stmt.setString(2, record.getIsbn());
            stmt.setDate(3, record.getBorrowDate());
            stmt.executeUpdate();
        }
    }

    /**
     * Xoá một bản ghi mượn sách theo userId và ISBN.
     *
     * @param conn   Kết nối đến cơ sở dữ liệu.
     * @param record Đối tượng {@link BorrowRecord} chứa userId và ISBN.
     * @throws SQLException Nếu có lỗi SQL.
     */
    public static void delete(Connection conn, BorrowRecord record) throws SQLException {
        String sql = "DELETE FROM borrow_records WHERE user_id = ? AND isbn = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, record.getUserId());
            stmt.setString(2, record.getIsbn());
            stmt.executeUpdate();
        }
    }

    /**
     * Kiểm tra xem một người dùng đã mượn tài liệu có ISBN nhất định hay chưa.
     *
     * @param conn   Kết nối đến cơ sở dữ liệu.
     * @param userId ID người dùng.
     * @param isbn   Mã ISBN của sách.
     * @return true nếu đã mượn, ngược lại false.
     * @throws SQLException Nếu có lỗi SQL.
     */
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

    /**
     * Truy vấn danh sách sách đã mượn của người dùng kèm thông tin tài liệu.
     *
     * @param conn   Kết nối đến cơ sở dữ liệu.
     * @param userId ID người dùng.
     * @return Danh sách {@link BorrowedBookInfo} gồm sách và thông tin mượn.
     * @throws SQLException Nếu có lỗi SQL.
     */
    public List<BorrowedBookInfo> getBorrowedBooksWithInfoByUserId(Connection conn, int userId) throws SQLException {
        List<BorrowedBookInfo> list = new ArrayList<>();
        String sql = """
                    SELECT br.isbn, br.borrow_date, br.return_date, br.status,
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
                    String status = rs.getString("status");

                    BorrowRecord record = new BorrowRecord(userId, isbn, borrowDate, returnDate, status);
                    Document document = new Document(title, isbn, thumbnailUrl);

                    list.add(new BorrowedBookInfo(document, record));
                }
            }
        }

        return list;
    }

    /**
     * Đánh dấu một sách đã được trả bằng cách cập nhật return_date thành ngày hiện tại.
     *
     * @param conn   Kết nối đến cơ sở dữ liệu.
     * @param userId ID người dùng.
     * @param isbn   ISBN của sách.
     * @throws SQLException Nếu có lỗi SQL.
     */
    public void markAsReturned(Connection conn, int userId, String isbn) throws SQLException {
        String update = "UPDATE borrow_records SET return_date = CURRENT_DATE WHERE user_id = ? AND isbn = ? AND return_date IS NULL";
        try (PreparedStatement stmt = conn.prepareStatement(update)) {
            stmt.setInt(1, userId);
            stmt.setString(2, isbn);
            stmt.executeUpdate();
        }
    }

    /**
     * Lấy toàn bộ danh sách bản ghi mượn từ bảng `borrow_record`.
     * (Lưu ý: Tên bảng này có thể sai — nên là `borrow_records` nếu thống nhất).
     *
     * @param conn Kết nối đến cơ sở dữ liệu.
     * @return Danh sách {@link BorrowRecord}.
     * @throws SQLException Nếu có lỗi SQL.
     */
    public List<BorrowRecord> getAllBorrowRecords(Connection conn) throws SQLException {
        List<BorrowRecord> borrowRecords = new ArrayList<>();
        String query = "SELECT * FROM borrow_record";  // Truy vấn tất cả bản ghi mượn

        try (
                PreparedStatement stmt = conn.prepareStatement(query)) {
            try (ResultSet rs = stmt.executeQuery()) {

                // Lấy kết quả trả về và tạo đối tượng BorrowRecord
                while (rs.next()) {
                    BorrowRecord record = new BorrowRecord(
                            rs.getInt("id"),
                            rs.getString("isbn"),
                            rs.getDate("borrow_date"),
                            rs.getDate("return_date"),
                            rs.getString("status"));
                    borrowRecords.add(record);
                }
            }
        }

        return borrowRecords;
    }

    /**
     * Cập nhật trạng thái bản ghi mượn dựa trên ID.
     *
     * @param conn   Kết nối đến cơ sở dữ liệu.
     * @param record Đối tượng {@link BorrowRecord} chứa thông tin mới.
     * @throws SQLException Nếu có lỗi SQL.
     */
    public void updateBorrowRecord(Connection conn, BorrowRecord record) throws SQLException {
        // Cập nhật bản ghi mượn trong cơ sở dữ liệu với trạng thái mới
        String updateQuery = "UPDATE borrow_records SET status = ? WHERE borrow_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
            stmt.setString(1, record.getStatus());
            stmt.setInt(2, record.getId());
            stmt.executeUpdate();
        }
    }
}
