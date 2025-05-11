// HistoryScreenController.java
package Controller;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import models.entities.BorrowedBookInfo;
import models.entities.User;
import models.services.BorrowRecordService;
import utils.AlertUtils;
import utils.SessionManager;

import java.sql.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HistoryScreenController {
    private final BorrowRecordService borrowService = new BorrowRecordService();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    @FXML
    private TableView<BorrowedBookInfo> historyTable;
    @FXML
    private TableColumn<BorrowedBookInfo, String> bookNameColumn;
    @FXML
    private TableColumn<BorrowedBookInfo, String> borrowDateColumn;
    @FXML
    private TableColumn<BorrowedBookInfo, String> returnDateColumn;
    @FXML
    private TableColumn<BorrowedBookInfo, String> statusColumn;
    @FXML
    private TableColumn<BorrowedBookInfo, String> remainingDaysColumn;

    @FXML
    public void initialize() {
        // Cài đặt cell value factory cho các cột
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

        remainingDaysColumn.setCellValueFactory(data -> {
            String status = data.getValue().getBorrowRecord().getStatus(); // Lấy trạng thái từ BorrowRecord
            if ("Đã trả".equals(status)) {
                return new SimpleStringProperty(""); // Trả về chuỗi rỗng nếu đã trả
            } else {
                String remainingDays = borrowService.getRemainingDays(data.getValue().getBorrowRecord());
                return new SimpleStringProperty(remainingDays); // Gọi service nếu đang mượn
            }
        });

        statusColumn.setCellValueFactory(data -> {
            Date returnDate = data.getValue().getBorrowRecord().getReturnDate();
            return new SimpleStringProperty(returnDate != null ? "Đã trả" : "Đang mượn");
        });

        // Tạo một task để lấy dữ liệu trong background
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            loadBorrowHistory(currentUser.getId()); // Tải dữ liệu lần đầu

            // Lên lịch cập nhật dữ liệu mỗi ngày một lần
            scheduler.scheduleAtFixedRate(() -> {
                loadBorrowHistory(currentUser.getId());
            }, 0, 1, TimeUnit.DAYS);
        }
    }

    private void loadBorrowHistory(int userId) {
        Task<List<BorrowedBookInfo>> loadHistoryTask = new Task<>() {
            @Override
            protected List<BorrowedBookInfo> call() {
                return borrowService.getBorrowedBooksByUserId(userId);
            }
        };

        loadHistoryTask.setOnSucceeded(event -> {
            List<BorrowedBookInfo> result = loadHistoryTask.getValue();
            historyTable.setItems(FXCollections.observableArrayList(result));
        });

        loadHistoryTask.setOnFailed(event -> {
            Throwable e = loadHistoryTask.getException();
            e.printStackTrace();
            AlertUtils.showAlert("Lỗi", "Lỗi tải lịch sử mượn sách", Alert.AlertType.ERROR);
        });

        Thread thread = new Thread(loadHistoryTask);
        thread.setDaemon(true);
        thread.start();
    }

    public void stopScheduler() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
    }
}