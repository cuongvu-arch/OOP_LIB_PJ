package models.viewmodel;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import models.entities.Document;

/**
 * View model đại diện cho thông tin đánh giá sách, dùng để hiển thị
 * trong các thành phần giao diện như TableView trong JavaFX.
 */
public class BookRatingView {
    private final Document book;
    private final StringProperty title;
    private final StringProperty isbn;

    /**
     * Khởi tạo một đối tượng BookRatingView từ đối tượng Document.
     *
     * @param book Đối tượng Document chứa thông tin sách.
     */
    public BookRatingView(Document book) {
        this.book = book;
        this.title = new SimpleStringProperty(book.getTitle());
        this.isbn = new SimpleStringProperty(book.getIsbn());
    }

    /**
     * Trả về {@link StringProperty} của tiêu đề sách để phục vụ binding với JavaFX.
     *
     * @return Thuộc tính tiêu đề dưới dạng {@link StringProperty}.
     */
    public StringProperty titleProperty() {
        return title;
    }

    /**
     * Trả về {@link StringProperty} của ISBN để phục vụ binding với JavaFX.
     *
     * @return Thuộc tính ISBN dưới dạng {@link StringProperty}.
     */
    public StringProperty isbnProperty() {
        return isbn;
    }
}
