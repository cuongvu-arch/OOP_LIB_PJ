package Controller;

import models.dao.DocumentDAO;
import models.entities.Document;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.ImageView;
import models.entities.User;
import models.services.GoogleBooksAPIService;
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
    private DocumentDAO documentDAO;
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
        documentDAO = new DocumentDAO();
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
            User currentUser = SessionManager.getCurrentUser();
            boolean isAdmin = currentUser != null && "admin".equalsIgnoreCase(currentUser.getRole());

            Document fetchedDoc = null;

            if (isAdmin) {
                try {
                    fetchedDoc = GoogleBooksAPIService.fetchBookInfo(isbn);
                    System.out.println("Fetched from API: " + (fetchedDoc != null ? fetchedDoc.getTitle() : "null"));
                } catch (Exception e) {
                    System.err.println("Google Books API error: " + e.getMessage());
                }
                if (fetchedDoc == null) {
                    fetchedDoc = documentDAO.getBookByIsbn(isbn);
                    System.out.println("Fetched from DB: " + (fetchedDoc != null ? fetchedDoc.getTitle() : "null"));
                }
            } else {
                fetchedDoc = documentDAO.getBookByIsbn(isbn);
                System.out.println("Fetched from DB (user): " + (fetchedDoc != null ? fetchedDoc.getTitle() : "null"));
            }

            if (fetchedDoc != null) {
                currentDocument = fetchedDoc;
                displayBookImageOnly(fetchedDoc);
                boolean existsInDb = documentDAO.bookExists(isbn);
                updateButtonStates(existsInDb, isAdmin);
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
            System.out.println("Loading image from: " + doc.getThumbnailUrl());
            BookImageLoader.loadImage(doc.getThumbnailUrl(), bookImageView);
            bookImageView.setVisible(true);
        } else {
            System.out.println("No thumbnail URL available for ISBN: " + doc.getIsbn());
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

    private void updateButtonStates(boolean existsInDb, boolean isAdmin) {
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
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null || !"admin".equalsIgnoreCase(currentUser.getRole())) {
            showAlert(AlertType.ERROR, "Lỗi", "Bạn không có quyền thực hiện thao tác này");
            return;
        }

        if (currentDocument == null || currentDocument.getIsbn() == null) {
            showAlert(AlertType.WARNING, "Thiếu thông tin", "Không có thông tin sách hợp lệ để thêm.");
            return;
        }

        String isbn = currentDocument.getIsbn();
        try {
            if (documentDAO.bookExists(isbn)) {
                showAlert(AlertType.INFORMATION, "Thông tin", "Sách này đã tồn tại trong kho.");
                addBookButton.setDisable(true);
                updateBookButton.setDisable(false);
                deleteBookButton.setDisable(false);
                return;
            }

            Document standardizedDoc = standardizeDocument(currentDocument);
            if (documentDAO.addBook(standardizedDoc)) {
                showAlert(AlertType.INFORMATION, "Thành công", "Đã thêm sách '" + standardizedDoc.getTitle() + "' vào kho.");
                resetUIState();
            } else {
                showAlert(AlertType.ERROR, "Lỗi", "Thêm sách thất bại. Kiểm tra console log.");
            }
        } catch (SQLException dbEx) {
            showAlert(AlertType.ERROR, "Lỗi cơ sở dữ liệu", "Lỗi khi kiểm tra/thêm sách: " + dbEx.getMessage());
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi hệ thống", "Đã xảy ra lỗi: " + e.getMessage());
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
            if (!documentDAO.bookExists(currentDocument.getIsbn())) {
                showAlert(AlertType.WARNING, "Cảnh báo", "Sách không còn tồn tại trong kho để cập nhật.");
                resetUIState();
                return;
            }

            Document standardizedDoc = standardizeDocument(currentDocument);
            if (documentDAO.updateBook(standardizedDoc)) {
                showAlert(AlertType.INFORMATION, "Thành công", "Đã cập nhật thông tin sách '" + standardizedDoc.getTitle() + "'.");
                addBookButton.setDisable(true);
                updateBookButton.setDisable(true);
                deleteBookButton.setDisable(true);
            } else {
                showAlert(AlertType.ERROR, "Lỗi", "Cập nhật sách thất bại. Kiểm tra console log.");
            }
        } catch (SQLException dbEx) {
            showAlert(AlertType.ERROR, "Lỗi cơ sở dữ liệu", "Lỗi khi cập nhật sách: " + dbEx.getMessage());
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi hệ thống", "Đã xảy ra lỗi: " + e.getMessage());
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
            if (!documentDAO.bookExists(isbn)) {
                showAlert(AlertType.WARNING, "Cảnh báo", "Sách không còn tồn tại trong kho để xóa.");
                resetUIState();
                return;
            }
            if (documentDAO.deleteBook(isbn)) {
                showAlert(AlertType.INFORMATION, "Thành công", "Đã xóa sách với ISBN: " + isbn + " khỏi kho.");
                resetUIState();
            } else {
                showAlert(AlertType.ERROR, "Lỗi", "Xóa sách thất bại. Kiểm tra console log.");
            }
        } catch (SQLException dbEx) {
            showAlert(AlertType.ERROR, "Lỗi cơ sở dữ liệu", "Lỗi khi xóa sách: " + dbEx.getMessage());
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi hệ thống", "Đã xảy ra lỗi: " + e.getMessage());
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

    private Document standardizeDocument(Document doc) {
        String publishDate = doc.getPublishedDate();
        if (publishDate != null && publishDate.matches("\\d{4}")) {
            publishDate = publishDate + "-01-01";
        } else if (publishDate == null || publishDate.trim().isEmpty()) {
            publishDate = null;
        }
        return new Document(doc.getIsbn(), doc.getTitle(), doc.getAuthors(), doc.getPublisher(),
                publishDate, doc.getDescription(), doc.getThumbnailUrl());
    }
}