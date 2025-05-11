package models.viewmodel;

import javafx.beans.property.SimpleStringProperty;

/**
 * Lớp view model đại diện cho thông tin sách đã mượn,
 * được sử dụng trong các thành phần giao diện JavaFX như TableView.
 */
public class BookBorrowedView {
    private final SimpleStringProperty display;

    /**
     * Khởi tạo đối tượng BookBorrowedView với tiêu đề và ISBN của sách.
     *
     * @param title Tiêu đề của sách.
     * @param isbn  Mã ISBN của sách.
     */
    public BookBorrowedView(String title, String isbn) {
        this.display = new SimpleStringProperty(title + " (" + isbn + ")");
    }

    /**
     * Trả về chuỗi hiển thị gồm tiêu đề và ISBN của sách.
     *
     * @return Chuỗi định dạng "Tiêu đề (ISBN)".
     */
    public String getDisplay() {
        return display.get();
    }

    /**
     * Trả về đối tượng {@link SimpleStringProperty} của chuỗi hiển thị,
     * dùng để binding với giao diện JavaFX.
     *
     * @return Thuộc tính display dưới dạng {@link SimpleStringProperty}.
     */
    public SimpleStringProperty displayProperty() {
        return display;
    }
}
