package models.viewmodel;

import javafx.beans.property.SimpleStringProperty;

public class UserBorrowView {
    private final SimpleStringProperty username;
    private final SimpleStringProperty borrowedBooks;
    private final SimpleStringProperty returnedBooks;

    public UserBorrowView(String username, String borrowedBooks, String returnedBooks) {
        this.username = new SimpleStringProperty(username);
        this.borrowedBooks = new SimpleStringProperty(borrowedBooks);
        this.returnedBooks = new SimpleStringProperty(returnedBooks);
    }

    public String getUsername() {
        return username.get();
    }

    public String getBorrowedBooks() {
        return borrowedBooks.get();
    }

    public String getReturnedBooks() {
        return returnedBooks.get();
    }
}
