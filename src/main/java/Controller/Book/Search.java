package Controller.Book;

import app.Document;
import models.DatabaseManagement.DocumentManagement; // Assuming this fetches from external source
import models.DatabaseManagement.BookManagement;
import models.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.sql.SQLException;

public class Search {
    @FXML private TextField isbnTextField;
    @FXML private TextArea resultTextArea;
    @FXML private Button searchButton; // Assuming you have a search button
    @FXML private Button addBookButton;
    @FXML private Button updateBookButton; // Add this button in your FXML
    @FXML private Button deleteBookButton; // Add this button in your FXML

    private Document currentDocument; // Holds the details of the book found (from external or potentially DB)
    private BookManagement bookManagement;

    @FXML
    private void initialize() {
        // Disable action buttons initially
        addBookButton.setDisable(true);
        updateBookButton.setDisable(true);
        deleteBookButton.setDisable(true);

        // Initialize BookManagement with database connection
        DatabaseConnection dbConnection = new DatabaseConnection(); // Handle potential connection errors if necessary
        bookManagement = new BookManagement(dbConnection);
    }

    @FXML
    private void handleSearchButtonClick() {
        String isbn = isbnTextField.getText().trim();
        if (isbn.isEmpty()) {
            showAlert(AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập ISBN để tìm kiếm.");
            resetUIState(); // Clear previous results and disable buttons
            return;
        }

        // Reset state before new search
        currentDocument = null;
        resultTextArea.clear();
        addBookButton.setDisable(true);
        updateBookButton.setDisable(true);
        deleteBookButton.setDisable(true);

        try {
            // 1. Attempt to fetch book info (e.g., from an external API via DocumentManagement)
            //    We prioritize external fetching to get potentially richer/updated data.
            //    If DocumentManagement directly queries your 'books' table, adjust logic.
            Document fetchedDoc = DocumentManagement.fetchBookInfo(isbn); // Assume this might return null

            if (fetchedDoc != null) {
                currentDocument = fetchedDoc; // Store the fetched document
                resultTextArea.setText(currentDocument.toString());

                // 2. Check if this book already exists in our local database
                boolean existsInDb = bookManagement.bookExists(isbn);

                if (existsInDb) {
                    // Book found externally AND exists in our DB: Enable Update/Delete
                    addBookButton.setDisable(true);
                    updateBookButton.setDisable(false);
                    deleteBookButton.setDisable(false);
                } else {
                    // Book found externally BUT NOT in our DB: Enable Add
                    addBookButton.setDisable(false);
                    updateBookButton.setDisable(true);
                    deleteBookButton.setDisable(true);
                }
            } else {
                // Book not found via external source. Optionally, you could try searching
                // your local DB here as a fallback, but current logic assumes external first.
                resultTextArea.setText("Không tìm thấy thông tin sách với ISBN: " + isbn);
                // Ensure all buttons remain disabled
                resetUIState(false); // Keep ISBN field, but clear results/buttons
            }

        } catch (SQLException dbEx) {
            showAlert(AlertType.ERROR, "Lỗi cơ sở dữ liệu", "Lỗi khi kiểm tra sách trong kho: " + dbEx.getMessage());
            resetUIState();
        }
        catch (Exception e) {
            // Catch broader exceptions from fetchBookInfo or other issues
            showAlert(AlertType.ERROR, "Lỗi hệ thống", "Đã xảy ra lỗi: " + e.getMessage());
            e.printStackTrace(); // Log the stack trace for debugging
            resetUIState();
        }
    }

    @FXML
    private void handleAddBookButtonClick() {
        if (currentDocument == null || currentDocument.getIsbn() == null) {
            showAlert(AlertType.WARNING, "Thiếu thông tin", "Không có thông tin sách hợp lệ để thêm.");
            return;
        }

        String isbn = currentDocument.getIsbn();

        try {
            // Double-check existence before adding (though search logic should prevent this state)
            if (bookManagement.bookExists(isbn)) {
                showAlert(AlertType.INFORMATION, "Thông tin", "Sách này đã tồn tại trong kho.");
                // Ensure correct button state
                addBookButton.setDisable(true);
                updateBookButton.setDisable(false);
                deleteBookButton.setDisable(false);
                return;
            }

            if (bookManagement.addBook(currentDocument)) {
                showAlert(AlertType.INFORMATION, "Thành công", "Đã thêm sách '" + currentDocument.getTitle() + "' vào kho.");
                resetUIState(); // Clear fields and disable buttons after successful add
            } else {
                // Error message should have been printed by BookManagement
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
            // Optional: Check if the book still exists before trying to update
            if (!bookManagement.bookExists(currentDocument.getIsbn())) {
                showAlert(AlertType.WARNING, "Cảnh báo", "Sách không còn tồn tại trong kho để cập nhật.");
                resetUIState();
                return;
            }

            if (bookManagement.updateBook(currentDocument)) {
                showAlert(AlertType.INFORMATION, "Thành công", "Đã cập nhật thông tin sách '" + currentDocument.getTitle() + "'.");
                // Optionally keep the data displayed or clear it
                // Reset buttons to require a new search for further actions
                addBookButton.setDisable(true); // Should already be disabled
                updateBookButton.setDisable(true);
                deleteBookButton.setDisable(true);
                // Maybe clear results? resultTextArea.clear();
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
            // Use ISBN from text field as fallback if currentDocument is somehow null but button enabled
            String isbn = isbnTextField.getText().trim();
            if (isbn.isEmpty()) {
                showAlert(AlertType.WARNING, "Thiếu thông tin", "Không có ISBN sách để xóa.");
                return;
            }
            // Ask for confirmation before deleting
            if (!showConfirmationDialog("Xác nhận xóa", "Bạn có chắc chắn muốn xóa sách với ISBN: " + isbn + "?")) {
                return;
            }
            performDelete(isbn);

        } else {
            String isbn = currentDocument.getIsbn();
            String title = currentDocument.getTitle() != null ? currentDocument.getTitle() : "không rõ tiêu đề";
            // Ask for confirmation before deleting
            if (!showConfirmationDialog("Xác nhận xóa", "Bạn có chắc chắn muốn xóa sách '" + title + "' (ISBN: " + isbn + ")?")) {
                return;
            }
            performDelete(isbn);
        }
    }

    private void performDelete(String isbn){
        try {
            // Optional: Check existence before deleting (though button state implies it exists)
            if (!bookManagement.bookExists(isbn)) {
                showAlert(AlertType.WARNING, "Cảnh báo", "Sách không còn tồn tại trong kho để xóa.");
                resetUIState();
                return;
            }

            if (bookManagement.deleteBook(isbn)) {
                showAlert(AlertType.INFORMATION, "Thành công", "Đã xóa sách với ISBN: " + isbn + " khỏi kho.");
                resetUIState(); // Clear fields and disable buttons
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


    /**
     * Resets the UI elements to their initial state.
     * @param clearIsbnField true to also clear the ISBN text field, false to keep it.
     */
    private void resetUIState(boolean clearIsbnField) {
        resultTextArea.clear();
        addBookButton.setDisable(true);
        updateBookButton.setDisable(true);
        deleteBookButton.setDisable(true);
        currentDocument = null;
        if (clearIsbnField) {
            isbnTextField.clear();
        }
    }

    /**
     * Overload for resetting UI and clearing ISBN field.
     */
    private void resetUIState() {
        resetUIState(true);
    }


    /**
     * Shows an Alert dialog.
     * @param alertType The type of alert (e.g., INFORMATION, ERROR, WARNING).
     * @param title The title of the alert window.
     * @param message The main message content of the alert.
     */
    private void showAlert(AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null); // No header text
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Shows a confirmation dialog.
     * @param title The title of the dialog.
     * @param message The confirmation question.
     * @return true if the user clicked OK, false otherwise.
     */
    private boolean showConfirmationDialog(String title, String message) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Show the dialog and wait for user response
        java.util.Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();

        // Return true only if the OK button was pressed
        return result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK;
    }
}