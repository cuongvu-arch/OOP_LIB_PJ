package models.entities;

import java.sql.Date;

public class BorrowRecord {
    private  int userId;
    private  String isbn;
    private  Date borrowDate;
    private  Date returnDate;// Thêm thuộc tính returnDate
    private int id;
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

    public BorrowRecord(int userId, String isbn, Date borrowDate, Date returnDate) {

        this.userId = userId;
        this.isbn = isbn;
        this.borrowDate = borrowDate;
        this.returnDate = returnDate;
    }

    public BorrowRecord() {

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getId() {
        return this.id;
    }

    public void setReturnDate(Date date) {

    }
}
