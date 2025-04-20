package models.DatabaseManagement;

import app.Document;
import models.DatabaseConnection;

import java.sql.*;

public class BookManagement {
    private DatabaseConnection connection;

    public BookManagement(DatabaseConnection connection) {
        this.connection = connection;
    }

    /**
     * Adds a new book to the database.
     * @param book The Document object representing the book.
     * @return true if the book was added successfully, false otherwise.
     */
    public boolean addBook(Document book) {
        // Input validation (basic)
        if (book == null || book.getIsbn() == null || book.getIsbn().trim().isEmpty()) {
            System.err.println("Lỗi khi thêm sách: Dữ liệu sách không hợp lệ (thiếu ISBN).");
            return false;
        }

        String sql = "INSERT INTO books (isbn, title, authors, publisher, published_date, description) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Convert authors array to comma-separated string
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
            // Check for duplicate key violation (specific error code might vary by database)
            if (e.getSQLState().startsWith("23")) { // Common SQLState for integrity constraint violation
                System.err.println("Lỗi khi thêm sách: ISBN '" + book.getIsbn() + "' đã tồn tại.");
            } else {
                System.err.println("Lỗi khi thêm sách: " + e.getMessage());
                e.printStackTrace(); // Print stack trace for debugging
            }
            return false;
        } catch (NullPointerException npe) {
            System.err.println("Lỗi khi thêm sách: Dữ liệu sách bị null. " + npe.getMessage());
            npe.printStackTrace();
            return false;
        }
    }

    /**
     * Updates an existing book in the database based on ISBN.
     * @param book The Document object containing the updated information.
     * @return true if the book was updated successfully, false otherwise.
     */
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
            // Return true if exactly one row was affected (or more, if ISBN wasn't unique, but it should be)
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

    /**
     * Deletes a book from the database based on ISBN.
     * @param isbn The ISBN of the book to delete.
     * @return true if the book was deleted successfully, false otherwise.
     */
    public boolean deleteBook(String isbn) {
        // Input validation
        if (isbn == null || isbn.trim().isEmpty()) {
            System.err.println("Lỗi khi xóa sách: ISBN không hợp lệ.");
            return false;
        }

        String sql = "DELETE FROM books WHERE isbn = ?";

        try (Connection conn = connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, isbn);

            int rowsAffected = stmt.executeUpdate();
            // Return true if exactly one row was deleted
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa sách (ISBN: " + isbn + "): " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


    /**
     * Checks if a book with the given ISBN exists in the database.
     * @param isbn The ISBN to check.
     * @return true if the book exists, false otherwise.
     * @throws SQLException if a database access error occurs.
     */
    public boolean bookExists(String isbn) throws SQLException {
        // Input validation
        if (isbn == null || isbn.trim().isEmpty()) {
            return false; // Or throw an IllegalArgumentException
        }
        String sql = "SELECT 1 FROM books WHERE isbn = ? LIMIT 1";
        try (Connection conn = connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, isbn);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next(); // Returns true if a row was found
            }
        }
        // Catch SQLException here or let it propagate as declared
        // Catching it here might be better for consistent error logging within this class
        // catch (SQLException e) {
        //     System.err.println("Lỗi khi kiểm tra sự tồn tại của sách (ISBN: " + isbn + "): " + e.getMessage());
        //     // Re-throwing or returning false depends on desired behavior
        //     throw e; // Re-throw to let the caller handle it
        // }
    }
}