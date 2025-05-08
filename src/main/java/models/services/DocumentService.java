package models.services;

import models.dao.DocumentDAO;
import models.data.DatabaseConnection;
import models.entities.Document;
import models.entities.User;
import org.json.JSONArray;
import org.json.JSONException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class DocumentService {
    private final DocumentDAO documentDAO;

    public DocumentService() {
        this.documentDAO = new DocumentDAO();
    }

    public Document searchBook(String isbn, User currentUser) throws SQLException, Exception {
        if (isbn == null || isbn.trim().isEmpty()) {
            return null;
        }

        boolean isAdmin = currentUser != null && "admin".equalsIgnoreCase(currentUser.getRole());
        Document fetchedDoc = null;

        if (isAdmin) {
            try {
                fetchedDoc = GoogleBooksAPIService.fetchBookInfo(isbn);
            } catch (Exception e) {
                System.err.println("Lỗi Google Books API: " + e.getMessage());
            }
            if (fetchedDoc == null) {
                fetchedDoc = documentDAO.getBookByIsbn(isbn);
            }
        } else {
            fetchedDoc = documentDAO.getBookByIsbn(isbn);
        }
        System.out.println("Tìm kiếm ISBN '" + isbn + "' -> Kết quả: " + (fetchedDoc != null ? fetchedDoc.getTitle() : "null"));
        return fetchedDoc;
    }

    public boolean bookExists(String isbn) throws SQLException {
        return documentDAO.bookExists(isbn);
    }

    public boolean addBook(Document document, User currentUser) throws SQLException {
        if (currentUser == null || !"admin".equalsIgnoreCase(currentUser.getRole())) {
            System.err.println("Thêm sách thất bại: người dùng không có quyền admin.");
            return false;
        }
        if (document == null || document.getIsbn() == null) {
            System.err.println("Thêm sách thất bại: thông tin sách không hợp lệ.");
            return false;
        }
        if (documentDAO.bookExists(document.getIsbn())) {
            System.err.println("Thêm sách thất bại: ISBN đã tồn tại.");
            return false;
        }
        Document standardizedDoc = standardizeDocument(document);
        return documentDAO.addBook(standardizedDoc);
    }

    public boolean updateBook(Document document, User currentUser) throws SQLException {
        if (currentUser == null || !"admin".equalsIgnoreCase(currentUser.getRole())) {
            System.err.println("Cập nhật sách thất bại: người dùng không có quyền admin.");
            return false;
        }
        if (document == null || document.getIsbn() == null) {
            System.err.println("Cập nhật sách thất bại: thông tin sách không hợp lệ.");
            return false;
        }
        if (!documentDAO.bookExists(document.getIsbn())) {
            System.err.println("Cập nhật sách thất bại: sách không tồn tại trong DB.");
            return false;
        }
        Document standardizedDoc = standardizeDocument(document);
        return documentDAO.updateBook(standardizedDoc);
    }

    public boolean deleteBook(String isbn, User currentUser) throws SQLException {
        if (currentUser == null || !"admin".equalsIgnoreCase(currentUser.getRole())) {
            System.err.println("Xóa sách thất bại: người dùng không có quyền admin.");
            return false;
        }
        if (isbn == null || isbn.trim().isEmpty()) {
            System.err.println("Xóa sách thất bại: ISBN không hợp lệ.");
            return false;
        }
        if (!documentDAO.bookExists(isbn)) {
            System.err.println("Xóa sách thất bại: sách không tồn tại trong DB.");
            return false;
        }
        return documentDAO.deleteBook(isbn);
    }

    private Document standardizeDocument(Document doc) {
        String publishDate = doc.getPublishedDate();
        if (publishDate != null && publishDate.matches("\\d{4}")) {
            publishDate = publishDate + "-01-01";
        } else if (publishDate == null || publishDate.trim().isEmpty() || "Không rõ".equalsIgnoreCase(publishDate)) {
            publishDate = null;
        }
        return new Document(doc.getIsbn(), doc.getTitle(), doc.getAuthors(), doc.getPublisher(),
                publishDate, doc.getDescription(), doc.getThumbnailUrl());
    }

    public List<Document> searchBooks(String title, String author, String publishDate) throws SQLException {
        List<Document> results = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM books WHERE 1=1");
        List<Object> params = new ArrayList<>();
        boolean performClientSideAuthorFilter = false;
        String clientSideAuthorQuery = null;

        if (title != null && !title.trim().isEmpty()) {
            sqlBuilder.append(" AND LOWER(title) LIKE LOWER(?)");
            params.add("%" + title.trim() + "%");
        }

        if (author != null && !author.trim().isEmpty()) {
            String trimmedAuthor = author.trim().toLowerCase();

            if (trimmedAuthor.length() <= 3) {
                performClientSideAuthorFilter = true;
                clientSideAuthorQuery = trimmedAuthor;

            } else {
                sqlBuilder.append(" AND LOWER(authors) LIKE LOWER(?)");
                params.add("%" + trimmedAuthor + "%");
            }
        }

        if (publishDate != null && !publishDate.trim().isEmpty()) {
            sqlBuilder.append(" AND publish_date LIKE ?");
            params.add(publishDate.trim() + "%");
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlBuilder.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String authorsJson = rs.getString("authors");
                String[] authorsArray;
                if (authorsJson != null && !authorsJson.isEmpty()) {
                    try {
                        JSONArray jsonAuthors = new JSONArray(authorsJson);
                        authorsArray = new String[jsonAuthors.length()];
                        for (int i = 0; i < jsonAuthors.length(); i++) {
                            authorsArray[i] = jsonAuthors.getString(i);
                        }
                    } catch (JSONException e) {
                        authorsArray = new String[]{authorsJson};
                        System.err.println("Lỗi parse JSON cho authors: " + authorsJson + " - " + e.getMessage());
                    }
                } else {
                    authorsArray = new String[0];
                }

                Document doc = new Document(
                        rs.getString("isbn"),
                        rs.getString("title"),
                        authorsArray,
                        rs.getString("publisher"),
                        rs.getString("publish_date"),
                        rs.getString("description"),
                        rs.getString("thumbnail_url")
                );

                if (performClientSideAuthorFilter) {
                    boolean match = false;
                    if (doc.getAuthors() != null) {
                        for (String anAuthor : doc.getAuthors()) {
                            if (anAuthor != null && anAuthor.toLowerCase().startsWith(clientSideAuthorQuery)) {
                                match = true;
                                break;
                            }
                        }
                    }
                    if (match) {
                        results.add(doc);
                    }
                } else {
                    results.add(doc);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi tìm kiếm sách: " + e.getMessage());
            throw e;
        }
        return results;
    }
}