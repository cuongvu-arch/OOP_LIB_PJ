package models.entities;

import java.sql.Date;

public class BorrowRecord {
    private int id;
    private final int userId;
    private final String isbn;
    private final Date borrowDate;
    private final Date returnDate;// Thêm thuộc tính returnDate
    private String status;

    public BorrowRecord(int userId, String isbn, Date borrowDate, Date returnDate, String status) {
        this.userId = userId;
        this.isbn = isbn;
        this.borrowDate = borrowDate;
        this.returnDate = returnDate;
        this.status = status;// Khởi tạo returnDate
    }

    public BorrowRecord(int userId, String isbn) {
        this(userId, isbn, new Date(System.currentTimeMillis()), null, "Đang mượn"); // Mặc định ngày mượn là ngày hiện tại
    }


    public int getUserId() {
        return userId;
    }

    public String getIsbn() {
        return isbn;
    }

    public Date getBorrowDate() {
        return borrowDate;
    }

    public Date getReturnDate() {
        return returnDate;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public int getId() {
        return this.id;
    }
}
