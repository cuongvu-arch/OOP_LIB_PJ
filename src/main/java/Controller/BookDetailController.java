package Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import models.entities.Document;
import utils.BookImageLoader;
import javafx.scene.image.Image;
import java.net.URL;

public class BookDetailController {

    @FXML private ImageView bookCoverImageView;
    @FXML private Text bookTitleText;
    @FXML private Text bookAuthorsText;
    @FXML private Text publishDateText;
    @FXML private Text publisherText;
    @FXML private Text isbnText;
    @FXML private Text languageText;
    @FXML private TextArea descriptionTextArea;
    @FXML private Button previewButton;
    @FXML private Button closeButton;

    private Document currentBook;

    public void initialize() {
    }

    public void setBookData(Document book) {
        this.currentBook = book;
        if (book == null) {
            System.err.println("Book data is null in BookDetailController.");
            bookTitleText.setText("Không có thông tin sách");
            return;
        }

        bookTitleText.setText(book.getTitle() != null ? book.getTitle() : "N/A");

        if (book.getAuthors() != null && book.getAuthors().length > 0) {
            bookAuthorsText.setText("bởi " + String.join(", ", book.getAuthors()));
        } else {
            bookAuthorsText.setText("bởi Tác giả không xác định");
        }

        publishDateText.setText(book.getPublishedDate() != null ? book.getPublishedDate() : "N/A");
        publisherText.setText(book.getPublisher() != null ? book.getPublisher() : "N/A");
        isbnText.setText(book.getIsbn() != null ? book.getIsbn() : "N/A");
        descriptionTextArea.setText(book.getDescription() != null && !book.getDescription().isEmpty() ? book.getDescription() : "Không có mô tả.");
        languageText.setText("Tiếng Anh");

        if (book.getThumbnailUrl() != null && !book.getThumbnailUrl().isEmpty()) {
            BookImageLoader.loadImage(book.getThumbnailUrl(), bookCoverImageView);
        } else {
            try {
                String placeholderPath = "/image/img.png";
                URL resourceUrl = getClass().getResource(placeholderPath);
                if (resourceUrl != null) {
                    bookCoverImageView.setImage(new Image(resourceUrl.toExternalForm()));
                } else {
                    System.err.println("Không tìm thấy ảnh placeholder: " + placeholderPath);
                }
            } catch (Exception e) {
                System.err.println("Lỗi tải ảnh placeholder: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handlePreviewButtonClick() {
        if (currentBook != null) {
            System.out.println("Nút xem trước được nhấn cho: " + currentBook.getTitle());
        }
    }

    @FXML
    private void handleCloseButtonClick() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}
