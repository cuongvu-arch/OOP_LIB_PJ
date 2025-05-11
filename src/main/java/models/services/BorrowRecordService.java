package models.services;

import models.dao.BorrowRecordDAO;
import models.data.DatabaseConnection;
import models.entities.BorrowRecord;
import models.entities.BorrowedBookInfo;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Service class that handles logic related to borrow records,
 * such as retrieving borrowed books, checking overdue status,
 * and updating borrow statuses.
 */
public class BorrowRecordService {
    private static final int MAX_BORROW_DAYS = 30;
    private final BorrowRecordDAO borrowRecordDAO = new BorrowRecordDAO();

    /**
     * Constructs a new {@code BorrowRecordService}.
     */
    public BorrowRecordService() {
    }

    /**
     * Retrieves all books that a specific user has borrowed and not yet returned.
     *
     * @param conn   the database connection
     * @param userId the ID of the user
     * @return list of unreturned borrowed book information
     * @throws SQLException if a database access error occurs
     */
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

    /**
     * Retrieves all books that a specific user has borrowed and already returned.
     *
     * @param conn   the database connection
     * @param userId the ID of the user
     * @return list of returned borrowed book information
     * @throws SQLException if a database access error occurs
     */
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

    /**
     * Retrieves all books that a user has borrowed (including returned and unreturned).
     *
     * @param userId the user ID
     * @return list of borrowed books
     */
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

    /**
     * Calculates how many days remain until a borrowed book is due.
     *
     * @param borrowRecord the borrow record
     * @return a string representing the remaining days or "Hết hạn" if overdue
     */
    public String getRemainingDays(BorrowRecord borrowRecord) {
        long borrowMillis = borrowRecord.getBorrowDate().getTime();
        long currentMillis = System.currentTimeMillis();
        long diffMillis = borrowMillis + (MAX_BORROW_DAYS * 24 * 60 * 60 * 1000L) - currentMillis;

        if (diffMillis < 0) {
            return "Hết hạn";  // Nếu quá hạn
        } else {
            long remainingDays = diffMillis / (24 * 60 * 60 * 1000L);  // Chuyển thành ngày
            return remainingDays + " ngày còn lại";
        }
    }

    /**
     * Checks all borrow records and updates the status to "Quá hạn" if any are overdue.
     */
    // Phương thức để cập nhật lại trạng thái mượn khi hết hạn
    public void checkAndUpdateBorrowStatus() {
        // Lấy tất cả các bản ghi mượn
        List<BorrowRecord> borrowRecords = getAllBorrowRecords();
        for (BorrowRecord record : borrowRecords) {
            if (isOverdue(record)) {
                // Cập nhật trạng thái của bản ghi mượn là "quá hạn"
                record.setStatus("Quá hạn");
                updateBorrowRecord(record);
            }
        }
    }

    /**
     * Checks if a given borrow record is overdue.
     *
     * @param record the borrow record
     * @return true if overdue, false otherwise
     */
    private boolean isOverdue(BorrowRecord record) {
        long borrowMillis = record.getBorrowDate().getTime();
        long currentMillis = System.currentTimeMillis();
        long diffMillis = currentMillis - borrowMillis;
        return diffMillis > (MAX_BORROW_DAYS * 24 * 60 * 60 * 1000L);  // Kiểm tra quá hạn
    }

    /**
     * Retrieves all borrow records from the database.
     *
     * @return list of all borrow records
     */
    private List<BorrowRecord> getAllBorrowRecords() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn != null) {
                return borrowRecordDAO.getAllBorrowRecords(conn);
            }
        } catch (SQLException e) {
            System.out.println("Error get Record" + e.getMessage());
        }
        return Collections.emptyList();
    }

    /**
     * Updates a specific borrow record in the database.
     *
     * @param record the borrow record to update
     */
    private void updateBorrowRecord(BorrowRecord record) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn != null) {
                borrowRecordDAO.updateBorrowRecord(conn, record);
            }
        } catch (SQLException e) {
            System.out.println("Error update Record" + e.getMessage());
        }
    }
}
