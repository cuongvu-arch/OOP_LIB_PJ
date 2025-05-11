package models.viewmodel;

import javafx.beans.property.SimpleStringProperty;

/**
 * View model đại diện cho thông tin mượn sách của người dùng,
 * sử dụng trong các bảng hiển thị như TableView trong JavaFX.
 */
public class UserBorrowView {
    private final SimpleStringProperty username;
    private final SimpleStringProperty borrowedBooks;
    private final SimpleStringProperty returnedBooks;

    /**
     * Khởi tạo một đối tượng UserBorrowView với thông tin người dùng và sách đã mượn/trả.
     *
     * @param username       Tên người dùng.
     * @param borrowedBooks  Danh sách sách đã mượn (dạng chuỗi).
     * @param returnedBooks  Danh sách sách đã trả (dạng chuỗi).
     */
    public UserBorrowView(String username, String borrowedBooks, String returnedBooks) {
        this.username = new SimpleStringProperty(username);
        this.borrowedBooks = new SimpleStringProperty(borrowedBooks);
        this.returnedBooks = new SimpleStringProperty(returnedBooks);
    }

    /**
     * Trả về tên người dùng.
     *
     * @return Tên người dùng dưới dạng chuỗi.
     */
    public String getUsername() {
        return username.get();
    }

    /**
     * Trả về danh sách sách đã mượn.
     *
     * @return Chuỗi mô tả sách đã mượn.
     */
    public String getBorrowedBooks() {
        return borrowedBooks.get();
    }

    /**
     * Trả về danh sách sách đã trả.
     *
     * @return Chuỗi mô tả sách đã trả.
     */
    public String getReturnedBooks() {
        return returnedBooks.get();
    }

    /**
     * Trả về thuộc tính tên người dùng để binding với JavaFX.
     *
     * @return {@link SimpleStringProperty} của tên người dùng.
     */
    public SimpleStringProperty usernameProperty() {
        return username;
    }

    /**
     * Trả về thuộc tính sách đã mượn để binding với JavaFX.
     *
     * @return {@link SimpleStringProperty} của sách đã mượn.
     */
    public SimpleStringProperty borrowedBooksProperty() {
        return borrowedBooks;
    }

    /**
     * Trả về thuộc tính sách đã trả để binding với JavaFX.
     *
     * @return {@link SimpleStringProperty} của sách đã trả.
     */
    public SimpleStringProperty returnedBooksProperty() {
        return returnedBooks;
    }
}
