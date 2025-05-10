package models.services;

import models.dao.BorrowRecordDAO;
import models.entities.BorrowedBookInfo;
import models.entities.Document;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BorrowHistoryService {
    private final BorrowRecordDAO borrowRecordDAO;

    public BorrowHistoryService(BorrowRecordDAO borrowRecordDAO) {
        this.borrowRecordDAO = borrowRecordDAO;
    }

    public List<BorrowedBookInfo> getUnreturnedBookInfo(Connection conn, int userId) throws SQLException {
        List<BorrowedBookInfo> all = borrowRecordDAO.getBorrowedBooksWithInfoByUserId(conn, userId);
        List<BorrowedBookInfo> result = new ArrayList<>();
        for (BorrowedBookInfo info : all) {
            if (info.getBorrowRecord().getReturnDate() == null) {
                result.add(info);
            }
        }
        return result;
    }

    public List<BorrowedBookInfo> getReturnedBookInfo(Connection conn, int userId) throws SQLException {
        List<BorrowedBookInfo> all = borrowRecordDAO.getBorrowedBooksWithInfoByUserId(conn, userId);
        List<BorrowedBookInfo> result = new ArrayList<>();
        for (BorrowedBookInfo info : all) {
            if (info.getBorrowRecord().getReturnDate() != null) {
                result.add(info);
            }
        }
        return result;
    }

}
