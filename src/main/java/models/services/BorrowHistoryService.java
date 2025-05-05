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

    public BorrowHistoryService(DocumentDAO documentDAO) {
        this.documentDAO = documentDAO;
    }

    public List<Document> getBorrowHistory(Connection conn, int userId) throws SQLException {
        List<BorrowRecord> records = BorrowRecordDAO.getByUserId(conn, userId);
        List<Document> documents = new ArrayList<>();

        for (BorrowRecord record : records) {
            Document doc = documentDAO.getBookByIsbn(record.getIsbn());
            if (doc != null) {
                documents.add(doc);
            }
        }

        return documents;
    }
}
