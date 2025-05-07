package models.dao;

import models.data.DatabaseConnection;
import models.entities.Document;
import org.json.JSONArray; // <-- Thêm import này

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
public class DocumentDAO {

    public boolean addBook(Document book) {
        if (book == null || book.getIsbn() == null || book.getIsbn().trim().isEmpty()) {
            System.err.println("Lỗi khi thêm sách: Dữ liệu sách không hợp lệ (thiếu ISBN).");
            return false;
        }

        String sql = "INSERT INTO books (isbn, title, authors, publisher, publish_date, description, thumbnail_url) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String authorsJsonString;
            List<String> authorsList = List.of(book.getAuthors());
            if (authorsList != null && !authorsList.isEmpty()) {
                JSONArray authorsJsonArray = new JSONArray(authorsList);
                authorsJsonString = authorsJsonArray.toString();
            } else {
                authorsJsonString = "[]";
            }

            stmt.setString(1, book.getIsbn());
            stmt.setString(2, book.getTitle());
            stmt.setString(3, authorsJsonString);
            stmt.setString(4, book.getPublisher());
            stmt.setString(5, book.getPublishedDate());
            stmt.setString(6, book.getDescription());
            stmt.setString(7, book.getThumbnailUrl()); // Thêm thumbnail_url

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
        } catch (Exception e) {
            System.err.println("Lỗi khi thêm sách: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


    public boolean updateBook(Document book) {
        if (book == null || book.getIsbn() == null || book.getIsbn().trim().isEmpty()) {
            System.err.println("Lỗi khi cập nhật sách: Dữ liệu sách không hợp lệ (thiếu ISBN).");
            return false;
        }

        String sql = "UPDATE books SET title = ?, authors = ?, publisher = ?, publish_date = ?, description = ?, thumbnail_url = ? " +
                "WHERE isbn = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String authorsJsonString;
            List<String> authorsList = List.of(book.getAuthors());
            if (authorsList != null && !authorsList.isEmpty()) {
                JSONArray authorsJsonArray = new JSONArray(authorsList);
                authorsJsonString = authorsJsonArray.toString();
            } else {
                authorsJsonString = "[]";
            }

            stmt.setString(1, book.getTitle());
            stmt.setString(2, authorsJsonString);
            stmt.setString(3, book.getPublisher());
            stmt.setString(4, book.getPublishedDate());
            stmt.setString(5, book.getDescription());
            stmt.setString(6, book.getThumbnailUrl()); // Thêm thumbnail_url
            stmt.setString(7, book.getIsbn());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật sách (ISBN: " + book.getIsbn() + "): " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("Lỗi khi cập nhật sách: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteBook(String isbn) {
        // Không cần thay đổi vì không thao tác với cột authors
        if (isbn == null || isbn.trim().isEmpty()) {
            System.err.println("Lỗi khi xóa sách: ISBN không hợp lệ.");
            return false;
        }

        String sql = "DELETE FROM books WHERE isbn = ?";

        try (Connection conn = DatabaseConnection.getConnection();
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
        String sql = "SELECT 1 FROM books WHERE isbn = ? LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, isbn);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public Document getBookByIsbn(String isbn) throws SQLException {
        String sql = "SELECT isbn, title, authors, publisher, publish_date, description, thumbnail_url " +
                "FROM books WHERE isbn = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, isbn);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String authorsJson = rs.getString("authors");
                String[] authors;
                try {
                    JSONArray authorsArray = new JSONArray(authorsJson);
                    authors = new String[authorsArray.length()];
                    for (int i = 0; i < authorsArray.length(); i++) {
                        authors[i] = authorsArray.getString(i);
                    }
                } catch (Exception e) {
                    authors = new String[0]; // Nếu JSON không hợp lệ
                }

                return new Document(
                        rs.getString("isbn"),
                        rs.getString("title"),
                        authors,
                        rs.getString("publisher"),
                        rs.getString("publish_date"),
                        rs.getString("description"),
                        rs.getString("thumbnail_url")
                );
            }
            return null;
        }
    }

    public static List<Document> getAllDocs(Connection conn) throws SQLException {
        List<Document> documents = new ArrayList<>();
        String sql = "SELECT isbn, title, authors, publisher, publish_date, description, thumbnail_url FROM books";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String authorsJson = rs.getString("authors");
                String[] authors;
                try {
                    JSONArray authorsArray = new JSONArray(authorsJson);
                    authors = new String[authorsArray.length()];
                    for (int i = 0; i < authorsArray.length(); i++) {
                        authors[i] = authorsArray.getString(i);
                    }
                } catch (Exception e) {
                    authors = new String[0]; // Nếu JSON không hợp lệ
                }

                Document document = new Document(
                        rs.getString("isbn"),
                        rs.getString("title"),
                        authors,
                        rs.getString("publisher"),
                        rs.getString("publish_date"),
                        rs.getString("description"),
                        rs.getString("thumbnail_url")
                );
                documents.add(document);
            }
        }
        return documents;
    }
    public List<Document> getBooksPaginated(Connection conn, int page, int booksPerPage) throws SQLException {
        List<Document> books = new ArrayList<>();
        int offset = (page - 1) * booksPerPage;

        String sql = "SELECT isbn, title, authors, publisher, publish_date, description, thumbnail_url " +
                "FROM books LIMIT ? OFFSET ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, booksPerPage);
            stmt.setInt(2, offset);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String authorsJson = rs.getString("authors");
                String[] authors;
                try {
                    JSONArray authorsArray = new JSONArray(authorsJson);
                    authors = new String[authorsArray.length()];
                    for (int i = 0; i < authorsArray.length(); i++) {
                        authors[i] = authorsArray.getString(i);
                    }
                } catch (Exception e) {
                    authors = new String[0];
                }

                Document book = new Document(
                        rs.getString("isbn"),
                        rs.getString("title"),
                        authors,
                        rs.getString("publisher"),
                        rs.getString("publish_date"),
                        rs.getString("description"),
                        rs.getString("thumbnail_url")
                );
                books.add(book);
            }
        }

        return books;
    }
}
