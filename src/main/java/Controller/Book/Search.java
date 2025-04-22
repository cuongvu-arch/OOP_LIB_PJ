package Controller.Book;

import app.Document;
import models.DatabaseManagement.DocumentManagement;
import models.DatabaseManagement.BookManagement;
import models.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import java.sql.SQLException;

public class Search {
    @FXML private TextField isbnTextField;
    @FXML private TextArea resultTextArea;
    @FXML private Button searchButton;
    @FXML private Button addBookButton;
    @FXML private Button updateBookButton;
    @FXML private Button deleteBookButton;
    @FXML private ImageView bookImageView;

    private Document currentDocument;
    private BookManagement bookManagement;

    @FXML
    private void initialize() {
        addBookButton.setDisable(true);
        updateBookButton.setDisable(true);
        deleteBookButton.setDisable(true);

        // Ẩn ImageView và TextArea ban đầu
        bookImageView.setVisible(false);
        resultTextArea.setVisible(false);

        // Thiết lập sự kiện click cho ImageView
        bookImageView.setOnMouseClicked(event -> handleImageClick());

        DatabaseConnection dbConnection = new DatabaseConnection();
        bookManagement = new BookManagement(dbConnection);
    }

    @FXML
    private void handleSearchButtonClick() {
        String isbn = isbnTextField.getText().trim();
        if (isbn.isEmpty()) {
            showAlert(AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập ISBN để tìm kiếm.");
            resetUIState();
            return;
        }

        // Reset state before new search
        currentDocument = null;
        resultTextArea.clear();
        addBookButton.setDisable(true);
        updateBookButton.setDisable(true);
        deleteBookButton.setDisable(true);
        bookImageView.setVisible(false);
        resultTextArea.setVisible(false);

        try {
            Document fetchedDoc = DocumentManagement.fetchBookInfo(isbn);

            if (fetchedDoc != null) {
                currentDocument = fetchedDoc;

                // Hiển thị hình ảnh nếu có
                if (currentDocument.getThumbnailUrl() != null && !currentDocument.getThumbnailUrl().isEmpty()) {
                    loadBookImage(currentDocument.getThumbnailUrl());
                    bookImageView.setVisible(true);
                } else {
                    // Nếu không có ảnh thì hiển thị luôn thông tin
                    resultTextArea.setText(currentDocument.toString());
                    resultTextArea.setVisible(true);
                }

                boolean existsInDb = bookManagement.bookExists(isbn);

                if (existsInDb) {
                    addBookButton.setDisable(true);
                    updateBookButton.setDisable(false);
                    deleteBookButton.setDisable(false);
                } else {
                    addBookButton.setDisable(false);
                    updateBookButton.setDisable(true);
                    deleteBookButton.setDisable(true);
                }
            } else {
                resultTextArea.setText("Không tìm thấy thông tin sách với ISBN: " + isbn);
                resultTextArea.setVisible(true);
                resetUIState(false);
            }

        } catch (SQLException dbEx) {
            showAlert(AlertType.ERROR, "Lỗi cơ sở dữ liệu", "Lỗi khi kiểm tra sách trong kho: " + dbEx.getMessage());
            resetUIState();
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi hệ thống", "Đã xảy ra lỗi: " + e.getMessage());
            e.printStackTrace();
            resetUIState();
        }
    }

    private void loadBookImage(String imageUrl) {
        try {
            Image image = new Image(imageUrl, true); // true = tải nền
            bookImageView.setImage(image);
            bookImageView.setPreserveRatio(true);
            bookImageView.setFitWidth(200);
            bookImageView.setFitHeight(300);

            // Xử lý khi tải ảnh lỗi
            image.errorProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    bookImageView.setImage(null);
                }
            });
        } catch (Exception e) {
            bookImageView.setImage(null);
        }
    }

    @FXML
    private void handleImageClick() {
        if (currentDocument != null) {
            resultTextArea.setText(currentDocument.toString());
            resultTextArea.setVisible(true);
        }
    }

    // Các phương thức còn lại giữ nguyên...
    @FXML
    private void handleAddBookButtonClick() {
        if (currentDocument == null || currentDocument.getIsbn() == null) {
            showAlert(AlertType.WARNING, "Thiếu thông tin", "Không có thông tin sách hợp lệ để thêm.");
            return;
        }

        String isbn = currentDocument.getIsbn();

        try {
            if (bookManagement.bookExists(isbn)) {
                showAlert(AlertType.INFORMATION, "Thông tin", "Sách này đã tồn tại trong kho.");
                addBookButton.setDisable(true);
                updateBookButton.setDisable(false);
                deleteBookButton.setDisable(false);
                return;
            }

            if (bookManagement.addBook(currentDocument)) {
                showAlert(AlertType.INFORMATION, "Thành công", "Đã thêm sách '" + currentDocument.getTitle() + "' vào kho.");
                resetUIState();
            } else {
                showAlert(AlertType.ERROR, "Lỗi", "Thêm sách thất bại. Kiểm tra console log để biết chi tiết.");
            }
        } catch (SQLException dbEx) {
            showAlert(AlertType.ERROR, "Lỗi cơ sở dữ liệu", "Lỗi khi kiểm tra/thêm sách: " + dbEx.getMessage());
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi hệ thống", "Đã xảy ra lỗi không mong muốn: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpdateBookButtonClick() {
        if (currentDocument == null || currentDocument.getIsbn() == null) {
            showAlert(AlertType.WARNING, "Thiếu thông tin", "Không có thông tin sách hợp lệ để cập nhật.");
            return;
        }

        try {
            if (!bookManagement.bookExists(currentDocument.getIsbn())) {
                showAlert(AlertType.WARNING, "Cảnh báo", "Sách không còn tồn tại trong kho để cập nhật.");
                resetUIState();
                return;
            }

            if (bookManagement.updateBook(currentDocument)) {
                showAlert(AlertType.INFORMATION, "Thành công", "Đã cập nhật thông tin sách '" + currentDocument.getTitle() + "'.");
                addBookButton.setDisable(true);
                updateBookButton.setDisable(true);
                deleteBookButton.setDisable(true);
            } else {
                showAlert(AlertType.ERROR, "Lỗi", "Cập nhật sách thất bại. Kiểm tra console log.");
            }
        } catch (SQLException dbEx) {
            showAlert(AlertType.ERROR, "Lỗi cơ sở dữ liệu", "Lỗi khi cập nhật sách: " + dbEx.getMessage());
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi hệ thống", "Đã xảy ra lỗi không mong muốn: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteBookButtonClick() {
        if (currentDocument == null || currentDocument.getIsbn() == null) {
            String isbn = isbnTextField.getText().trim();
            if (isbn.isEmpty()) {
                showAlert(AlertType.WARNING, "Thiếu thông tin", "Không có ISBN sách để xóa.");
                return;
            }

            if (!showConfirmationDialog("Xác nhận xóa", "Bạn có chắc chắn muốn xóa sách với ISBN: " + isbn + "?")) {
                return;
            }
            performDelete(isbn);
        } else {
            String isbn = currentDocument.getIsbn();
            String title = currentDocument.getTitle() != null ? currentDocument.getTitle() : "không rõ tiêu đề";

            if (!showConfirmationDialog("Xác nhận xóa", "Bạn có chắc chắn muốn xóa sách '" + title + "' (ISBN: " + isbn + ")?")) {
                return;
            }
            performDelete(isbn);
        }
    }

    private void performDelete(String isbn) {
        try {
            if (!bookManagement.bookExists(isbn)) {
                showAlert(AlertType.WARNING, "Cảnh báo", "Sách không còn tồn tại trong kho để xóa.");
                resetUIState();
                return;
            }

            if (bookManagement.deleteBook(isbn)) {
                showAlert(AlertType.INFORMATION, "Thành công", "Đã xóa sách với ISBN: " + isbn + " khỏi kho.");
                resetUIState();
            } else {
                showAlert(AlertType.ERROR, "Lỗi", "Xóa sách thất bại. Kiểm tra console log.");
            }
        } catch (SQLException dbEx) {
            showAlert(AlertType.ERROR, "Lỗi cơ sở dữ liệu", "Lỗi khi xóa sách: " + dbEx.getMessage());
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi hệ thống", "Đã xảy ra lỗi không mong muốn: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void resetUIState(boolean clearIsbnField) {
        resultTextArea.clear();
        resultTextArea.setVisible(false);
        bookImageView.setImage(null);
        bookImageView.setVisible(false);
        addBookButton.setDisable(true);
        updateBookButton.setDisable(true);
        deleteBookButton.setDisable(true);
        currentDocument = null;
        if (clearIsbnField) {
            isbnTextField.clear();
        }
    }

    private void resetUIState() {
        resetUIState(true);
    }

    private void showAlert(AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean showConfirmationDialog(String title, String message) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        java.util.Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();

        return result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK;
    }
}