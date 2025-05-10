package Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.entities.Document;
import models.entities.User;
import models.services.DocumentService;
import utils.BookImageLoader;
import utils.SessionManager;

import java.io.IOException;
import java.sql.SQLException;

public class BookSearchController {
    @FXML
    private TextField isbnTextField;
    @FXML
    private TextArea resultTextArea;
    @FXML
    private Button searchButton;
    @FXML
    private Button addBookButton;
    @FXML
    private Button updateBookButton;
    @FXML
    private Button deleteBookButton;
    @FXML
    private ImageView bookImageView;

    private Document currentDocument;
    private DocumentService documentService;
    private User currentUser;

    @FXML
    private void initialize() {
        documentService = new DocumentService();
        this.currentUser = SessionManager.getCurrentUser();
        updateButtonVisibility();

        bookImageView.setVisible(false);
        resultTextArea.setVisible(false);
        addBookButton.setDisable(true);
        updateBookButton.setDisable(true);
        deleteBookButton.setDisable(true);

        // Thay đổi sự kiện click để mở cửa sổ chi tiết
        bookImageView.setOnMouseClicked(event -> {
            if (currentDocument != null) {
                openBookDetailWindow(currentDocument);
            }
        });
    }

    private void openBookDetailWindow(Document book) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/BookDetailScreen.fxml"));
            Parent root = loader.load();

            BookDetailController controller = loader.getController();
            controller.setBookData(book);

            Stage detailStage = new Stage();
            detailStage.setTitle("Chi tiết sách: " + (book.getTitle() != null ? book.getTitle() : "Không có tiêu đề"));
            detailStage.setScene(new Scene(root));
            detailStage.initModality(Modality.APPLICATION_MODAL);
            detailStage.setResizable(false);
            detailStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở trang chi tiết sách: " + e.getMessage());
        }
    }

    public void setUser(User user) {
        this.currentUser = user;
        updateButtonVisibility();
        if (currentDocument != null) {
            try {
                boolean existsInDb = documentService.bookExists(currentDocument.getIsbn());
                updateAdminButtonStates(existsInDb);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            updateAdminButtonStates(false);
        }
    }

    private void updateButtonVisibility() {
        boolean isAdmin = currentUser != null && "admin".equalsIgnoreCase(currentUser.getRole());
        addBookButton.setVisible(isAdmin);
        updateBookButton.setVisible(isAdmin);
        deleteBookButton.setVisible(isAdmin);
    }

    @FXML
    private void handleSearchButtonClick() {
        String isbn = isbnTextField.getText().trim();
        if (isbn.isEmpty()) {
            showAlert(AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập ISBN để tìm kiếm.");
            resetUIStateAfterSearch(true);
            return;
        }

        if (!isValidIsbn(isbn)) {
            showAlert(AlertType.WARNING, "ISBN không hợp lệ", "Vui lòng nhập ISBN 10 hoặc 13 chữ số hợp lệ.");
            return;
        }

        resetUIStateAfterSearch(false);
        searchButton.setDisable(true);

        try {
            Document fetchedDoc = documentService.searchBook(isbn, this.currentUser);
            if (fetchedDoc != null) {
                currentDocument = fetchedDoc;
                displayBookImageOnly(fetchedDoc);
                boolean existsInDb = documentService.bookExists(isbn);
                updateAdminButtonStates(existsInDb);
            } else {
                showAlert(AlertType.INFORMATION, "Không tìm thấy", "Không tìm thấy sách với ISBN: " + isbn);
                bookImageView.setVisible(false);
                resultTextArea.setVisible(false);
                updateAdminButtonStates(false);
            }
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Lỗi cơ sở dữ liệu", "Không thể truy vấn: " + e.getMessage());
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
            BookImageLoader.loadImage(doc.getThumbnailUrl(), bookImageView);
            bookImageView.setVisible(true);
        } else {
            bookImageView.setImage(null);
            bookImageView.setVisible(false);
            System.out.println("Không có URL ảnh cho ISBN: " + doc.getIsbn());
        }
        resultTextArea.setVisible(false);
    }

    private void updateAdminButtonStates(boolean existsInDb) {
        boolean isAdmin = currentUser != null && "admin".equalsIgnoreCase(currentUser.getRole());
        if (!isAdmin || currentDocument == null) {
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
            if (documentService.addBook(currentDocument, this.currentUser)) {
                showAlert(AlertType.INFORMATION, "Thành công", "Đã thêm sách '" + currentDocument.getTitle() + "' vào kho.");
                updateAdminButtonStates(true);
            } else {
                showAlert(AlertType.ERROR, "Lỗi", "Thêm sách thất bại. Vui lòng kiểm tra quyền hoặc thông tin sách (có thể ISBN đã tồn tại).");
            }
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Lỗi cơ sở dữ liệu", "Lỗi khi thêm sách: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpdateBookButtonClick() {
        if (currentDocument == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn sách", "Vui lòng tìm và chọn một cuốn sách để cập nhật.");
            return;
        }

        if (this.currentUser == null || !"admin".equalsIgnoreCase(this.currentUser.getRole())) {
            showAlert(Alert.AlertType.ERROR, "Không có quyền", "Chỉ quản trị viên mới có thể cập nhật sách.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EditBookScreen.fxml"));
            Parent root = loader.load();

            EditBookController controller = loader.getController();
            controller.setBookToEdit(currentDocument);

            Stage editStage = new Stage();
            editStage.setTitle("Chỉnh sửa sách: " + currentDocument.getTitle());
            editStage.setScene(new Scene(root));
            editStage.initModality(Modality.APPLICATION_MODAL);
            editStage.setResizable(false);
            editStage.showAndWait();

            try {
                Document updatedDoc = documentService.searchBook(currentDocument.getIsbn(), this.currentUser);
                if (updatedDoc != null) {
                    this.currentDocument = updatedDoc;
                    if (resultTextArea.isVisible()) {
                        resultTextArea.setText(this.currentDocument.toString());
                    }
                    displayBookImageOnly(this.currentDocument);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Không thể tải lại thông tin sách sau khi cập nhật.");
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi tải giao diện", "Không thể mở trang chỉnh sửa sách: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteBookButtonClick() {
        if (currentDocument == null || currentDocument.getIsbn() == null) {
            showAlert(AlertType.WARNING, "Thiếu thông tin", "Không có ISBN sách để xóa.");
            return;
        }

        String isbn = currentDocument.getIsbn();
        String title = (currentDocument.getTitle() != null) ? currentDocument.getTitle() : "không rõ tiêu đề";

        if (!showConfirmationDialog("Xác nhận xóa", "Bạn có chắc chắn muốn xóa sách '" + title + "' (ISBN: " + isbn + ")?")) {
            return;
        }

        try {
            if (documentService.deleteBook(isbn, this.currentUser)) {
                showAlert(AlertType.INFORMATION, "Thành công", "Đã xóa sách với ISBN: " + isbn + " khỏi kho.");
                resetUIStateAfterSearch(true);
            } else {
                showAlert(AlertType.ERROR, "Lỗi", "Xóa sách thất bại. Vui lòng kiểm tra quyền hoặc thông tin sách.");
            }
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Lỗi cơ sở dữ liệu", "Lỗi khi xóa sách: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void resetUIStateAfterSearch(boolean clearIsbnField) {
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
        if (isbn == null) return false;
        String cleanedIsbn = isbn.replaceAll("[^0-9X]", "");
        return cleanedIsbn.length() == 10 || cleanedIsbn.length() == 13;
    }
}