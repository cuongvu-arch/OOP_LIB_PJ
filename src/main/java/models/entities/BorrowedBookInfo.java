package models.entities;

import java.sql.Date;

/**
 * Lớp kết hợp thông tin của một tài liệu (Document) và bản ghi mượn tương ứng (BorrowRecord).
 * Dùng để hiển thị hoặc xử lý thông tin sách đã được mượn bởi người dùng.
 */
public class UserBorrowJoinInfo {
    private int userId;
    private String username;
    private String bookTitle;
    private String isbn;
    private Date borrowDate;
    private Date returnDate;

    /**
     * Tạo một đối tượng BorrowedBookInfo mới với đầy đủ thông tin.
     */
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
