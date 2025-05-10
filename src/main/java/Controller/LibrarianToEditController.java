package Controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import models.dao.DocumentDAO;
import models.data.DatabaseConnection;
import models.entities.DocumentWithBorrowInfo;
import models.services.DocumentService;

import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class LibrarianToEditController {

    private final ObservableList<DocumentWithBorrowInfo> books = FXCollections.observableArrayList();
    public javafx.scene.control.Button adjustButton;
    @FXML
    private TableView<DocumentWithBorrowInfo> bookTable;
    @FXML
    private TableColumn<DocumentWithBorrowInfo, String> isbnColumn;
    @FXML
    private TableColumn<DocumentWithBorrowInfo, String> titleColumn;
    @FXML
    private TableColumn<DocumentWithBorrowInfo, Integer> quantityColumn;
    @FXML
    private TableColumn<DocumentWithBorrowInfo, Integer> borrowedColumn;
    @FXML
    private TableColumn<DocumentWithBorrowInfo, Integer> availableColumn;
    @FXML
    private javafx.scene.control.TextField adjustIsbnField;
    @FXML
    private javafx.scene.control.TextField adjustQuantityField;

    @FXML
    public void initialize() {
        // Liên kết cột với thuộc tính
        isbnColumn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("totalQuantity"));
        borrowedColumn.setCellValueFactory(new PropertyValueFactory<>("currentlyBorrowed"));
        availableColumn.setCellValueFactory(new PropertyValueFactory<>("availableQuantity"));

        // Gán danh sách vào bảng
        bookTable.setItems(books);

        // Tải dữ liệu từ database
        loadBooksFromDatabase();
    }

    private void loadBooksFromDatabase() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Lấy danh sách sách với thông tin số lượng mượn, tổng số lượng và số lượng còn lại
            List<DocumentWithBorrowInfo> list = DocumentDAO.getAllDocumentsWithBorrowInfo(conn);
            books.setAll(list); // Cập nhật ObservableList để bảng hiển thị
        } catch (SQLException e) {
            e.printStackTrace(); // Bạn có thể hiển thị thông báo lỗi ở đây, hoặc cảnh báo người dùng
        }
    }

    @FXML
    private void handleAdjustQuantity() {
        String isbn = adjustIsbnField.getText().trim();
        String quantityText = adjustQuantityField.getText().trim();

        if (isbn.isEmpty() || quantityText.isEmpty()) {
            showAlert("Thông báo", "Bạn cần nhập ISBN và số lượng thay đổi.");
            return;
        }

        try {
            int quantityChange = Integer.parseInt(quantityText);
            if (quantityChange == 0) {
                showAlert("Thông báo", "Số lượng thay đổi phải khác 0.");
                return;
            }

            DocumentService.adjustBookQuantity(isbn, quantityChange);
            showAlert("Thành công", "Cập nhật số lượng sách thành công.");
            loadBooksFromDatabase();

        } catch (NumberFormatException e) {
            showAlert("Lỗi", "Số lượng phải là số nguyên.");
        } catch (IllegalArgumentException e) {
            showAlert("Cảnh báo", e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Lỗi", "Lỗi khi cập nhật sách.");
        }
    }

    public void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
