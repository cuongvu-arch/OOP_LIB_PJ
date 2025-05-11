package Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import models.entities.Document;
import models.entities.User;
import models.services.DocumentService;
import utils.BookImageLoader;
import utils.SessionManager;

import java.net.URL;
import java.sql.SQLException;

/**
 * Controller cho giao diện chỉnh sửa thông tin sách.
 * Cho phép admin cập nhật các thông tin như tiêu đề, tác giả, ngày xuất bản, mô tả,...
 */
public class EditBookController {

    @FXML
    private ImageView bookCoverImageView;
    @FXML
    private TextField isbnTextField;
    @FXML
    private TextField titleTextField;
    @FXML
    private TextField authorsTextField;
    @FXML
    private TextField publisherTextField;
    @FXML
    private TextField publishDateTextField;
    @FXML
    private TextField thumbnailUrlTextField;
    @FXML
    private TextArea descriptionTextArea;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;

    private Document currentBook; // Sách đang được chỉnh sửa
    private DocumentService documentService;

    /**
     * Khởi tạo controller, gán listener cho trường URL ảnh để cập nhật ảnh bìa khi thay đổi.
     */
    public void initialize() {
        documentService = new DocumentService();
        thumbnailUrlTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.trim().isEmpty()) {
                BookImageLoader.loadImage(newValue.trim(), bookCoverImageView);
            } else {
                loadPlaceholderImage();
            }
        });
    }

    /**
     * Gán sách cần chỉnh sửa vào form.
     *
     * @param book đối tượng Document cần chỉnh sửa.
     */
    public void setBookToEdit(Document book) {
        this.currentBook = book;
        if (book == null) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không có thông tin sách để chỉnh sửa.");
            closeWindow();
            return;
        }

        isbnTextField.setText(book.getIsbn());
        titleTextField.setText(book.getTitle());
        if (book.getAuthors() != null) {
            authorsTextField.setText(String.join(", ", book.getAuthors()));
        } else {
            authorsTextField.setText("");
        }
        publisherTextField.setText(book.getPublisher());
        publishDateTextField.setText(book.getPublishedDate());
        thumbnailUrlTextField.setText(book.getThumbnailUrl());
        descriptionTextArea.setText(book.getDescription());

        if (book.getThumbnailUrl() != null && !book.getThumbnailUrl().isEmpty()) {
            BookImageLoader.loadImage(book.getThumbnailUrl(), bookCoverImageView);
        } else {
            loadPlaceholderImage();
        }
    }

    /**
     * Tải ảnh placeholder nếu URL không hợp lệ hoặc trống.
     */

    private void loadPlaceholderImage() {
        try {
            String placeholderPath = "/image/img.png";
            URL resourceUrl = getClass().getResource(placeholderPath);
            if (resourceUrl != null) {
                bookCoverImageView.setImage(new Image(resourceUrl.toExternalForm()));
            } else {
                System.err.println("Lỗi: Không tìm thấy tệp ảnh placeholder tại '" + placeholderPath + "' trong EditBookController.");
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi tải ảnh placeholder '" + "/image/img.png" + "' trong EditBookController: " + e.getMessage());
        }
    }

    /**
     * Xử lý khi nhấn nút Lưu: kiểm tra quyền, validate và cập nhật sách.
     */
    @FXML
    private void handleSaveButtonClick() {
        if (this.currentBook == null) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không có sách nào đang được chọn để lưu.");
            return;
        }

        String title = titleTextField.getText().trim();
        String[] authors = authorsTextField.getText().trim().isEmpty() ? new String[0] : authorsTextField.getText().trim().split("\\s*,\\s*");
        String publisher = publisherTextField.getText().trim();
        String publishDate = publishDateTextField.getText().trim();
        String thumbnailUrl = thumbnailUrlTextField.getText().trim();
        String description = descriptionTextArea.getText().trim();

        if (title.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Tiêu đề sách không được để trống.");
            return;
        }

        Document updatedBookData = new Document(
                this.currentBook.getIsbn(),
                title,
                authors,
                publisher,
                publishDate,
                description,
                thumbnailUrl
        );

        try {
            User currentUser = SessionManager.getCurrentUser();
            if (currentUser == null || !"admin".equalsIgnoreCase(currentUser.getRole())) {
                showAlert(Alert.AlertType.ERROR, "Lỗi quyền", "Bạn không có quyền thực hiện hành động này.");
                return;
            }

            if (documentService.updateBook(updatedBookData, currentUser)) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Thông tin sách đã được cập nhật thành công!");
                this.currentBook = updatedBookData; // Cập nhật lại sách hiện tại trong controller này
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Cập nhật thông tin sách thất bại.");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi cơ sở dữ liệu", "Lỗi khi cập nhật sách: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống", "Đã xảy ra lỗi không mong muốn: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Xử lý khi nhấn nút Hủy: đóng cửa sổ chỉnh sửa.
     */
    @FXML
    private void handleCancelButtonClick() {
        closeWindow();
    }


    /**
     * Đóng cửa sổ hiện tại.
     */
    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Hiển thị một hộp thoại cảnh báo/thông báo cho người dùng.
     *
     * @param alertType loại cảnh báo.
     * @param title     tiêu đề hộp thoại.
     * @param message   nội dung thông báo.
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}