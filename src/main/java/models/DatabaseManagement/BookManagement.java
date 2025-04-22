package models.DatabaseManagement;

import app.Document;
import models.DatabaseConnection;
import org.json.JSONArray; // <-- Thêm import này

import java.sql.*;
import java.util.List; // <-- Thêm import này nếu chưa có

public class BookManagement {
    private DatabaseConnection connection;

    public BookManagement(DatabaseConnection connection) {
        this.connection = connection;
    }

    public boolean addBook(Document book) {

        if (book == null || book.getIsbn() == null || book.getIsbn().trim().isEmpty()) {
            System.err.println("Lỗi khi thêm sách: Dữ liệu sách không hợp lệ (thiếu ISBN).");
            return false;
        }

        // Câu lệnh SQL không đổi
        String sql = "INSERT INTO books (isbn, title, authors, publisher, publish_date, description) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // *** THAY ĐỔI Ở ĐÂY: Chuyển đổi danh sách authors thành chuỗi JSON ***
            String authorsJsonString;
            List<String> authorsList = List.of(book.getAuthors()); // Lấy danh sách tác giả
            if (authorsList != null && !authorsList.isEmpty()) {
                JSONArray authorsJsonArray = new JSONArray(authorsList); // Tạo JSONArray từ List
                authorsJsonString = authorsJsonArray.toString();      // Chuyển thành chuỗi, ví dụ: ["Author A", "Author B"]
            } else {
                authorsJsonString = "[]"; // Nếu không có tác giả, lưu mảng JSON rỗng
            }
            // *** KẾT THÚC THAY ĐỔI ***

            stmt.setString(1, book.getIsbn());
            stmt.setString(2, book.getTitle());
            stmt.setString(3, authorsJsonString); // <-- Gửi chuỗi JSON vào cột authors
            stmt.setString(4, book.getPublisher());
            stmt.setString(5, book.getPublishedDate()); // Đảm bảo hàm này trả về chuỗi ngày hợp lệ
            stmt.setString(6, book.getDescription());

            // Dòng thực thi không đổi (gần dòng 38 cũ)
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            // Xử lý lỗi không đổi
            if (e.getSQLState().startsWith("23")) { // Mã lỗi cho vi phạm ràng buộc (như unique key)
                System.err.println("Lỗi khi thêm sách: ISBN '" + book.getIsbn() + "' đã tồn tại.");
            } else {
                System.err.println("Lỗi khi thêm sách: " + e.getMessage());
                e.printStackTrace(); // In đầy đủ stack trace để debug các lỗi khác
            }
            return false;
        } catch (NullPointerException npe) { // Bắt lỗi NullPointer nếu cần
            System.err.println("Lỗi khi thêm sách: Dữ liệu sách bị null. " + npe.getMessage());
            npe.printStackTrace();
            return false;
        }
        // Cân nhắc thêm catch (org.json.JSONException e) nếu có thao tác JSON phức tạp hơn
    }


    public boolean updateBook(Document book) {
        // Input validation không đổi
        if (book == null || book.getIsbn() == null || book.getIsbn().trim().isEmpty()) {
            System.err.println("Lỗi khi cập nhật sách: Dữ liệu sách không hợp lệ (thiếu ISBN).");
            return false;
        }

        // Câu lệnh SQL không đổi (đã sửa publish_date)
        String sql = "UPDATE books SET title = ?, authors = ?, publisher = ?, publish_date = ?, description = ? " +
                "WHERE isbn = ?";

        try (Connection conn = connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // *** THAY ĐỔI Ở ĐÂY: Chuyển đổi danh sách authors thành chuỗi JSON ***
            String authorsJsonString;
            List<String> authorsList = List.of(book.getAuthors());
            if (authorsList != null && !authorsList.isEmpty()) {
                JSONArray authorsJsonArray = new JSONArray(authorsList);
                authorsJsonString = authorsJsonArray.toString();
            } else {
                authorsJsonString = "[]"; // Mảng JSON rỗng
            }
            // *** KẾT THÚC THAY ĐỔI ***

            stmt.setString(1, book.getTitle());
            stmt.setString(2, authorsJsonString); // <-- Gửi chuỗi JSON vào cột authors (vị trí thứ 2)
            stmt.setString(3, book.getPublisher());
            stmt.setString(4, book.getPublishedDate());
            stmt.setString(5, book.getDescription());
            stmt.setString(6, book.getIsbn()); // ISBN cho điều kiện WHERE

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật sách (ISBN: " + book.getIsbn() + "): " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (NullPointerException npe) {
            System.err.println("Lỗi khi cập nhật sách: Dữ liệu sách bị null. " + npe.getMessage());
            npe.printStackTrace();
            return false;
        }
        // Cân nhắc thêm catch (org.json.JSONException e)
    }


    public boolean deleteBook(String isbn) {
        // Không cần thay đổi vì không thao tác với cột authors
        if (isbn == null || isbn.trim().isEmpty()) {
            System.err.println("Lỗi khi xóa sách: ISBN không hợp lệ.");
            return false;
        }

        String sql = "DELETE FROM books WHERE isbn = ?";

        try (Connection conn = connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, isbn);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa sách (ISBN: " + isbn + "): " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean bookExists(String isbn) throws SQLException {
        // Không cần thay đổi
        if (isbn == null || isbn.trim().isEmpty()) {
            return false; // Hoặc ném IllegalArgumentException
        }
        String sql = "SELECT 1 FROM books WHERE isbn = ? LIMIT 1";
        try (Connection conn = connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, isbn);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next(); // Trả về true nếu tìm thấy, false nếu không
            }
        }
        // Không cần catch SQLException ở đây nếu phương thức khai báo throws SQLException
    }
}