package models.entities;

public class BorrowRecord {
    private int userId;
    private String isbn;

    public BorrowRecord(int userId, String isbn) {
        this.userId = userId;
        this.isbn = isbn;
    }

    public int getUserId() {
        return userId;
    }

    public String getIsbn() {
        return isbn;
    }
}
