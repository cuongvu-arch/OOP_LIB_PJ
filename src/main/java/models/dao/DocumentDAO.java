package models.dao;

import models.data.DatabaseConnection;
import models.entities.Document;
import models.entities.DocumentWithBorrowInfo;
import org.json.JSONArray; // <-- Thêm import này

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DocumentDAO {

    public static int getQuantityByIsbn(Connection conn, String isbn) throws SQLException {
        String sql = "SELECT total_quantity FROM books WHERE isbn = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, isbn);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total_quantity");
            } else {
                throw new SQLException("Không tìm thấy sách với ISBN: " + isbn);
            }
        }
    }

    public static void updateBookQuantity(Connection conn, String isbn, int quantityChange) throws SQLException {
        String sql = "UPDATE books SET total_quantity = total_quantity + ? WHERE isbn = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quantityChange);
            stmt.setString(2, isbn);
            stmt.executeUpdate();
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
                        rs.getString("thumbnail_url"),
                        rs.getString("qr_code_path")
                );
                documents.add(document);
            }
        }
        return documents;
    }

    public static List<DocumentWithBorrowInfo> getAllDocumentsWithBorrowInfo(Connection conn) throws SQLException {
        List<DocumentWithBorrowInfo> list = new ArrayList<>();

        String sql = """
                    SELECT d.isbn, d.title, d.thumbnail_url, d.total_quantity,
                           COUNT(br.isbn) AS currently_borrowed,
                           (d.total_quantity - COUNT(br.isbn)) AS available_quantity
                    FROM books d
                    LEFT JOIN borrow_records br
                        ON d.isbn = br.isbn AND br.return_date IS NULL
                    GROUP BY d.isbn, d.title, d.thumbnail_url, d.total_quantity
                    ORDER BY d.title
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String isbn = rs.getString("isbn");
                String title = rs.getString("title");
                String thumbnailUrl = rs.getString("thumbnail_url");
                int totalQuantity = rs.getInt("total_quantity");
                int currentlyBorrowed = rs.getInt("currently_borrowed");
                int availableQuantity = rs.getInt("available_quantity");

                DocumentWithBorrowInfo doc = new DocumentWithBorrowInfo(
                        title, isbn, thumbnailUrl, totalQuantity, currentlyBorrowed, availableQuantity
                );

                list.add(doc);
            }
        }

        return list;
    }

    public boolean addBook(Document book) {
        if (book == null || book.getIsbn() == null || book.getIsbn().trim().isEmpty()) {
            System.err.println("Lỗi khi thêm sách: Dữ liệu sách không hợp lệ (thiếu ISBN).");
            return false;
        }

        String sql = "INSERT INTO books (isbn, title, authors, publisher, publish_date, description, thumbnail_url, qr_code_path) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String authorsJsonString;
            List<String> authorsList = book.getAuthors() != null ? List.of(book.getAuthors()) : List.of();
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
            stmt.setString(7, book.getThumbnailUrl());
            stmt.setString(8, book.getQrCodePath());// Thêm thumbnail_url

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
        String sql = "SELECT isbn, title, authors, publisher, publish_date, description, thumbnail_url, qr_code_path, google_books_url, total_quantity FROM books WHERE isbn = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, isbn);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String authorsJson = rs.getString("authors");
                String[] authors;
                if (authorsJson != null && !authorsJson.isEmpty()) {
                    JSONArray jsonAuthors = new JSONArray(authorsJson);
                    authors = new String[jsonAuthors.length()];
                    for (int i = 0; i < jsonAuthors.length(); i++) {
                        authors[i] = jsonAuthors.getString(i);
                    }
                } else {
                    authors = new String[0];
                }
                Document doc = new Document(
                        rs.getString("isbn"),
                        rs.getString("title"),
                        authors,
                        rs.getString("publisher"),
                        rs.getString("publish_date"),
                        rs.getString("description"),
                        rs.getString("thumbnail_url")
                );
                String qrCodePath = rs.getString("qr_code_path");
                if (qrCodePath == null || qrCodePath.isEmpty()) {
                    System.err.println("Warning: qr_code_path is null or empty for ISBN: " + isbn);
                }
                doc.setQrCodePath(qrCodePath);
                doc.setGoogleBooksUrl(rs.getString("google_books_url"));
                doc.setQuantity(rs.getInt("total_quantity"));
                return doc;
            } else {
                System.err.println("No book found for ISBN: " + isbn);
                return null;
            }
        } catch (SQLException e) {
            System.err.println("SQL error fetching book for ISBN: " + isbn + ": " + e.getMessage());
            throw e;
        }
    }

    public List<Document> getBooksPaginated(Connection conn, int page, int booksPerPage) throws SQLException {
        List<Document> books = new ArrayList<>();
        int offset = (page - 1) * booksPerPage;

        String sql = "SELECT isbn, title, authors, publisher, publish_date, description, thumbnail_url, qr_code_path " +
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

                String qrCodePath = rs.getString("qr_code_path");

                Document book = new Document(
                        rs.getString("isbn"),
                        rs.getString("title"),
                        authors,
                        rs.getString("publisher"),
                        rs.getString("publish_date"),
                        rs.getString("description"),
                        rs.getString("thumbnail_url"),
                        qrCodePath
                );
                books.add(book);
            }
        } catch (SQLException e) {
            System.err.println("SQL error in getBooksPaginated: " + e.getMessage());
            throw e;
        }

        return books;
    }
}
