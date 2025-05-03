package models.services;

import models.dao.DocumentDAO;
import models.entities.Document;
import models.entities.User;
import models.services.GoogleBooksAPIService;
import java.sql.SQLException;

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
                System.out.println("Lấy từ API: " + (fetchedDoc != null ? fetchedDoc.getTitle() : "null"));
            } catch (Exception e) {
                System.err.println("Lỗi Google Books API: " + e.getMessage());
            }
            if (fetchedDoc == null) {
                fetchedDoc = documentDAO.getBookByIsbn(isbn);
                System.out.println("Lấy từ DB: " + (fetchedDoc != null ? fetchedDoc.getTitle() : "null"));
            }
        } else {
            fetchedDoc = documentDAO.getBookByIsbn(isbn);
            System.out.println("Lấy từ DB (người dùng): " + (fetchedDoc != null ? fetchedDoc.getTitle() : "null"));
        }

        return fetchedDoc;
    }

    public boolean bookExists(String isbn) throws SQLException {
        return documentDAO.bookExists(isbn);
    }

    public boolean addBook(Document document, User currentUser) throws SQLException {
        if (currentUser == null || !"admin".equalsIgnoreCase(currentUser.getRole())) {
            return false;
        }
        if (document == null || document.getIsbn() == null || documentDAO.bookExists(document.getIsbn())) {
            return false;
        }
        Document standardizedDoc = standardizeDocument(document);
        return documentDAO.addBook(standardizedDoc);
    }

    public boolean updateBook(Document document, User currentUser) throws SQLException {
        if (currentUser == null || !"admin".equalsIgnoreCase(currentUser.getRole())) {
            return false;
        }
        if (document == null || document.getIsbn() == null || !documentDAO.bookExists(document.getIsbn())) {
            return false;
        }
        Document standardizedDoc = standardizeDocument(document);
        return documentDAO.updateBook(standardizedDoc);
    }

    public boolean deleteBook(String isbn, User currentUser) throws SQLException {
        if (currentUser == null || !"admin".equalsIgnoreCase(currentUser.getRole())) {
            return false;
        }
        if (isbn == null || !documentDAO.bookExists(isbn)) {
            return false;
        }
        return documentDAO.deleteBook(isbn);
    }

    private Document standardizeDocument(Document doc) {
        String publishDate = doc.getPublishedDate();
        if (publishDate != null && publishDate.matches("\\d{4}")) {
            publishDate = publishDate + "-01-01";
        } else if (publishDate == null || publishDate.trim().isEmpty()) {
            publishDate = null;
        }
        return new Document(doc.getIsbn(), doc.getTitle(), doc.getAuthors(), doc.getPublisher(),
                publishDate, doc.getDescription(), doc.getThumbnailUrl());
    }
}