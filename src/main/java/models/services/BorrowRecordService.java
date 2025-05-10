package models.services;

import models.dao.BorrowRecordDAO;
import models.data.DatabaseConnection;
import models.entities.BorrowedBookInfo;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BorrowRecordService {
    private final BorrowRecordDAO borrowRecordDAO = new BorrowRecordDAO();


    public BorrowRecordService() {
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

    public List<BorrowedBookInfo> getBorrowedBooksByUserId(int userId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn != null) {
                return borrowRecordDAO.getBorrowedBooksWithInfoByUserId(conn, userId);
            }
        } catch (SQLException e) {
            System.err.println("Error loading borrow history: " + e.getMessage());
        }
        return Collections.emptyList();
    }
}
