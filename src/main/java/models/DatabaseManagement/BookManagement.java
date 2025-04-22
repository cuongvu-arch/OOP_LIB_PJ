package models.DatabaseManagement;

import app.Document;
import models.DatabaseConnection;

import java.sql.*;

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

        String sql = "INSERT INTO books (isbn, title, authors, publisher, published_date, description) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {


            String authorsString = (book.getAuthors() != null) ? String.join(", ", book.getAuthors()) : "";

            stmt.setString(1, book.getIsbn());
            stmt.setString(2, book.getTitle());
            stmt.setString(3, authorsString);
            stmt.setString(4, book.getPublisher());
            stmt.setString(5, book.getPublishedDate());
            stmt.setString(6, book.getDescription());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {

            if (e.getSQLState().startsWith("23")) {
                System.err.println("Lỗi khi thêm sách: ISBN '" + book.getIsbn() + "' đã tồn tại.");
            } else {
                System.err.println("Lỗi khi thêm sách: " + e.getMessage());
                e.printStackTrace();
            }
            return false;
        } catch (NullPointerException npe) {
            System.err.println("Lỗi khi thêm sách: Dữ liệu sách bị null. " + npe.getMessage());
            npe.printStackTrace();
            return false;
        }
    }


    public boolean updateBook(Document book) {
        // Input validation (basic)
        if (book == null || book.getIsbn() == null || book.getIsbn().trim().isEmpty()) {
            System.err.println("Lỗi khi cập nhật sách: Dữ liệu sách không hợp lệ (thiếu ISBN).");
            return false;
        }

        String sql = "UPDATE books SET title = ?, authors = ?, publisher = ?, published_date = ?, description = ? " +
                "WHERE isbn = ?";

        try (Connection conn = connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String authorsString = (book.getAuthors() != null) ? String.join(", ", book.getAuthors()) : "";

            stmt.setString(1, book.getTitle());
            stmt.setString(2, authorsString);
            stmt.setString(3, book.getPublisher());
            stmt.setString(4, book.getPublishedDate());
            stmt.setString(5, book.getDescription());
            stmt.setString(6, book.getIsbn()); // WHERE clause parameter

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
    }


    public boolean deleteBook(String isbn) {

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

        if (isbn == null || isbn.trim().isEmpty()) {
            return false;
        }
        String sql = "SELECT 1 FROM books WHERE isbn = ? LIMIT 1";
        try (Connection conn = connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, isbn);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }

    }
}