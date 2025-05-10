// HistoryScreenController.java
package Controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import models.entities.BorrowedBookInfo;
import models.entities.User;
import models.services.BorrowRecordService;
import utils.SessionManager;

import java.sql.Date;
import java.util.List;

public class HistoryScreenController {
    @FXML private TableView<BorrowedBookInfo> historyTable;
    @FXML private TableColumn<BorrowedBookInfo, String> bookNameColumn;
    @FXML private TableColumn<BorrowedBookInfo, String> borrowDateColumn;
    @FXML private TableColumn<BorrowedBookInfo, String> returnDateColumn;
    @FXML private TableColumn<BorrowedBookInfo, String> statusColumn;

    private final BorrowRecordService borrowService = new BorrowRecordService();

    @FXML
    public void initialize() {
        bookNameColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getDocument().getTitle())
        );

        borrowDateColumn.setCellValueFactory(cellData -> {
            Date borrowDate = cellData.getValue().getBorrowDate();
            return new SimpleStringProperty(borrowDate != null ? borrowDate.toString() : "N/A");
        });

        returnDateColumn.setCellValueFactory(data -> {
            Date returnDate = data.getValue().getBorrowRecord().getReturnDate();
            return new SimpleStringProperty(returnDate != null ? returnDate.toString() : "Chưa trả");
        });

        statusColumn.setCellValueFactory(data -> {
            Date returnDate = data.getValue().getBorrowRecord().getReturnDate();
            return new SimpleStringProperty(returnDate != null ? "Đã trả" : "Đang mượn");
        });

        User currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            List<BorrowedBookInfo> list = borrowService.getBorrowedBooksByUserId(currentUser.getId());
            historyTable.setItems(FXCollections.observableArrayList(list));
        }
    }
}
