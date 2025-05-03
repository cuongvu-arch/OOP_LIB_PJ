package Controller;

import models.entities.Document;
import models.services.DocumentService;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.ImageView;
import models.entities.User;
import utils.BookImageLoader;
import utils.SessionManager;

import java.sql.SQLException;

public class BookSearchController {
    @FXML private TextField isbnTextField;
    @FXML private TextArea resultTextArea;
    @FXML private Button searchButton;
    @FXML private Button addBookButton;
    @FXML private Button updateBookButton;
    @FXML private Button deleteBookButton;
    @FXML private ImageView bookImageView;

    private Document currentDocument;
    private DocumentService documentService;
    private User currentUser;

    public void setUser(User user) {
        this.currentUser = user;
        updateButtonVisibility();
    }

    private void updateButtonVisibility() {
        boolean isAdmin = currentUser != null && "admin".equalsIgnoreCase(currentUser.getRole());
        addBookButton.setVisible(isAdmin);
        updateBookButton.setVisible(isAdmin);
        deleteBookButton.setVisible(isAdmin);
    }

    @FXML
    private void initialize() {
        documentService = new DocumentService();
        bookImageView.setVisible(false);
        resultTextArea.setVisible(false);
        bookImageView.setOnMouseClicked(event -> handleImageClick());
    }

    @FXML
    private void handleSearchButtonClick() {
        String isbn = isbnTextField.getText().trim();
        if (isbn.isEmpty()) {
            showAlert(AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập ISBN để tìm kiếm.");
            resetUIState();
            return;
        }

        if (!isValidIsbn(isbn)) {
            showAlert(AlertType.WARNING, "ISBN không hợp lệ", "Vui lòng nhập ISBN 10 hoặc 13 chữ số hợp lệ.");
            resetUIState();
            return;
        }

        resetUIState(false);
        searchButton.setDisable(true);

        try {
            Document fetchedDoc = documentService.searchBook(isbn, currentUser);
            if (fetchedDoc != null) {
                currentDocument = fetchedDoc;
                displayBookImageOnly(fetchedDoc);
                boolean existsInDb = documentService.bookExists(isbn);
                updateButtonStates(existsInDb);
            } else {
                showAlert(AlertType.INFORMATION, "Không tìm thấy", "Không tìm thấy sách với ISBN: " + isbn);
                resetUIState(false);
            }
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Lỗi", "Không thể truy vấn cơ sở dữ liệu: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi hệ thống", "Đã xảy ra lỗi: " + e.getMessage());
            e.printStackTrace();
        } finally {
            searchButton.setDisable(false);
        }
    }

    private void displayBookImageOnly(Document doc) {
        if (doc.getThumbnailUrl() != null && !doc.getThumbnailUrl().isEmpty()) {
            System.out.println("Tải ảnh từ: " + doc.getThumbnailUrl());
            BookImageLoader.loadImage(doc.getThumbnailUrl(), bookImageView);
            bookImageView.setVisible(true);
        } else {
            System.out.println("Không có URL ảnh cho ISBN: " + doc.getIsbn());
            bookImageView.setVisible(false);
        }
        resultTextArea.setVisible(false);
    }

    @FXML
    private void handleImageClick() {
        if (currentDocument != null) {
            resultTextArea.setText(currentDocument.toString());
            resultTextArea.setVisible(true);
        }
    }

    private void updateButtonStates(boolean existsInDb) {
        boolean isAdmin = currentUser != null && "admin".equalsIgnoreCase(currentUser.getRole());
        if (!isAdmin) {
            addBookButton.setDisable(true);
            updateBookButton.setDisable(true);
            deleteBookButton.setDisable(true);
            return;
        }
        addBookButton.setDisable(existsInDb);
        updateBookButton.setDisable(!existsInDb);
        deleteBookButton.setDisable(!existsInDb);
    }

    @FXML
    private void handleAddBookButtonClick() {
        if (currentDocument == null || currentDocument.getIsbn() == null) {
            showAlert(AlertType.WARNING, "Thiếu thông tin", "Không có thông tin sách hợp lệ để thêm.");
            return;
        }

        try {
            if (documentService.addBook(currentDocument, currentUser)) {
                showAlert(AlertType.INFORMATION, "Thành công", "Đã thêm sách '" + currentDocument.getTitle() + "' vào kho.");
                resetUIState();
            } else {
                showAlert(AlertType.ERROR, "Lỗi", "Thêm sách thất bại. Vui lòng kiểm tra quyền hoặc thông tin sách.");
            }
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Lỗi cơ sở dữ liệu", "Lỗi khi thêm sách: " + e.getMessage());
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
            if (documentService.updateBook(currentDocument, currentUser)) {
                showAlert(AlertType.INFORMATION, "Thành công", "Đã cập nhật thông tin sách '" + currentDocument.getTitle() + "'.");
                resetUIState();
            } else {
                showAlert(AlertType.ERROR, "Lỗi", "Cập nhật sách thất bại. Vui lòng kiểm tra quyền hoặc thông tin sách.");
            }
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Lỗi cơ sở dữ liệu", "Lỗi khi cập nhật sách: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteBookButtonClick() {
        String isbn = isbnTextField.getText().trim();
        if (isbn.isEmpty() && (currentDocument == null || currentDocument.getIsbn() == null)) {
            showAlert(AlertType.WARNING, "Thiếu thông tin", "Không có ISBN sách để xóa.");
            return;
        }

        isbn = (currentDocument != null && currentDocument.getIsbn() != null) ? currentDocument.getIsbn() : isbn;
        String title = (currentDocument != null && currentDocument.getTitle() != null) ? currentDocument.getTitle() : "không rõ tiêu đề";

        if (!showConfirmationDialog("Xác nhận xóa", "Bạn có chắc chắn muốn xóa sách '" + title + "' (ISBN: " + isbn + ")?")) {
            return;
        }

        try {
            if (documentService.deleteBook(isbn, currentUser)) {
                showAlert(AlertType.INFORMATION, "Thành công", "Đã xóa sách với ISBN: " + isbn + " khỏi kho.");
                resetUIState();
            } else {
                showAlert(AlertType.ERROR, "Lỗi", "Xóa sách thất bại. Vui lòng kiểm tra quyền hoặc thông tin sách.");
            }
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Lỗi cơ sở dữ liệu", "Lỗi khi xóa sách: " + e.getMessage());
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
        if (clearIsbnField) isbnTextField.clear();
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
        return alert.showAndWait().filter(response -> response == javafx.scene.control.ButtonType.OK).isPresent();
    }

    private boolean isValidIsbn(String isbn) {
        isbn = isbn.replaceAll("[^0-9X]", "");
        return isbn.length() == 10 || isbn.length() == 13;
    }
}