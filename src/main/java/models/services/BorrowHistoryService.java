package models.services;

import models.dao.BorrowRecordDAO;
import models.dao.DocumentDAO;
import models.entities.BorrowRecord;
import models.entities.Document;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BorrowHistoryService {
    private final DocumentDAO documentDAO;
    private final BorrowRecordDAO borrowRecordDAO;

    public BorrowHistoryService(DocumentDAO documentDAO, BorrowRecordDAO borrowRecordDAO) {
        this.documentDAO = documentDAO;
        this.borrowRecordDAO = borrowRecordDAO;
    }

    public List<Document> getReturnedBooks(Connection conn, int userId) throws SQLException {
        List<BorrowRecord> records = borrowRecordDAO.getByUserId(conn, userId);
        List<Document> returnedDocuments = new ArrayList<>();

        for (BorrowRecord record : records) {
            // Kiểm tra xem có ngày trả hay không
            if (record.getReturnDate() != null) {
                Document doc = documentDAO.getBookByIsbn(record.getIsbn());
                if (doc != null) {
                    returnedDocuments.add(doc);
                }
            }
        }

        return returnedDocuments;
    }

    // Lấy các sách chưa trả
    public List<Document> getUnreturnedBooks(Connection conn, int userId) throws SQLException {
        List<BorrowRecord> records = borrowRecordDAO.getByUserId(conn, userId);
        List<Document> unreturnedDocuments = new ArrayList<>();

        for (BorrowRecord record : records) {
            // Kiểm tra xem chưa có ngày trả
            if (record.getReturnDate() == null) {
                Document doc = documentDAO.getBookByIsbn(record.getIsbn());
                if (doc != null) {
                    unreturnedDocuments.add(doc);
                }
            }
        }

        return unreturnedDocuments;
    }
}

