package Controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import models.dao.DocumentDAO;
import models.data.DatabaseConnection;
import models.entities.DocumentWithBorrowInfo;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class LibrarianToEditController {

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

    private final ObservableList<DocumentWithBorrowInfo> books = FXCollections.observableArrayList();

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
}
