package models.entities;

import java.sql.Date;

/**
 * Represents a record of a document borrowed by a user.
 * Contains information about the user, document (by ISBN), borrow and return dates, and status.
 */
public class BorrowRecord {
    private int userId;
    private String isbn;
    private Date borrowDate;
    private Date returnDate;
    private int id;
    private String status;

    /**
     * Full constructor for creating a BorrowRecord with all fields.
     *
     * @param userId     the ID of the user borrowing the document
     * @param isbn       the ISBN of the document
     * @param borrowDate the date the document was borrowed
     * @param returnDate the date the document was returned (nullable if not returned)
     * @param status     the status of the borrow (e.g., "Đang mượn", "Đã trả")
     */
    public BorrowRecord(int userId, String isbn, Date borrowDate, Date returnDate, String status) {
        this.userId = userId;
        this.isbn = isbn;
        this.borrowDate = borrowDate;
        this.returnDate = returnDate;
        this.status = status;
    }

    /**
     * Constructor for a new borrow, defaulting to current date and status "Đang mượn".
     *
     * @param userId the ID of the user
     * @param isbn   the ISBN of the document
     */
    public BorrowRecord(int userId, String isbn) {
        this(userId, isbn, new Date(System.currentTimeMillis()), null, "Đang mượn");
    }

    /**
     * Default constructor.
     */
    public BorrowRecord() {
    }

    public BorrowRecord(int id, String isbn001, Object o, Object o1) {
    }

    /**
     * @return the ID of the user who borrowed the document
     */
    public int getUserId() {
        return userId;
    }

    /**
     * @return the ISBN of the borrowed document
     */
    public String getIsbn() {
        return isbn;
    }

    /**
     * @return the date the document was borrowed
     */
    public Date getBorrowDate() {
        return this.borrowDate;
    }

    /**
     * @return the date the document was returned, or null if not yet returned
     */
    public Date getReturnDate() {
        return this.returnDate;
    }

    /**
     * @return the current status of the borrow (e.g., "Đang mượn", "Đã trả")
     */
    public String getStatus() {
        return status;
    }

    /**
     * Updates the status of the borrow record.
     *
     * @param status the new status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return the internal ID of the borrow record (if set)
     */
    public int getId() {
        return this.id;
    }

    public void setReturnDate(Object o) {

    }

    public void setBorrowDate(Date date) {

    }
}
