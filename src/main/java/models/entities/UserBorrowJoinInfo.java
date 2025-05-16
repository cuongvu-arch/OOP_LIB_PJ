package models.entities;

import java.sql.Date;

public class UserBorrowJoinInfo {
    private int userId;
    private String username;
    private String bookTitle;
    private String isbn;
    private Date borrowDate;
    private Date returnDate;

    public UserBorrowJoinInfo(int userId, String username, String bookTitle, String isbn,
                              Date borrowDate, Date returnDate) {
        this.userId = userId;
        this.username = username;
        this.bookTitle = bookTitle;
        this.isbn = isbn;
        this.borrowDate = borrowDate;
        this.returnDate = returnDate;
    }

    // Getters

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getBookTitle() {
        return bookTitle;
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
}
