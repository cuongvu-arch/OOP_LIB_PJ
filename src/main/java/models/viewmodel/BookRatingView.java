package models.viewmodel;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import models.entities.Document;
import javafx.scene.image.Image;


/**
 * View model đại diện cho thông tin đánh giá sách, dùng để hiển thị
 * trong các thành phần giao diện như TableView trong JavaFX.
 */
public class BookRatingView {
    private final Document book;
    private final StringProperty title;
    private final StringProperty isbn;
    private final StringProperty thumbnailUrl;

    /**
     * Khởi tạo một đối tượng BookRatingView từ đối tượng Document.
     *
     * @param book Đối tượng Document chứa thông tin sách.
     */
    public BookRatingView(Document book) {
        this.book = book;
        this.title = new SimpleStringProperty(book.getTitle());
        this.isbn = new SimpleStringProperty(book.getIsbn());
        this.thumbnailUrl = new SimpleStringProperty(book.getThumbnailUrl());
    }

    public Image getThumbnailImage() {
        String url = thumbnailUrl.get();
        
        if (url == null || url.isEmpty()) {
            System.out.println("Thumbnail URL is null or empty");
            return null;
        }

        Image image = new Image(url, true);
        image.errorProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                System.err.println("Failed to load image: " + url);
            }
        });

        return image;
    }


    public StringProperty titleProperty() {
        return title;
    }

    public StringProperty isbnProperty() {
        return isbn;
    }

    public StringProperty thumbnailUrlProperty() {
        return thumbnailUrl;
    }

    /**
     * Trả về đối tượng Document gốc để mở chi tiết sách khi cần.
     */
    public Document getDocument() {
        return book;
    }
}
