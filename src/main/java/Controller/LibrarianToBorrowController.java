package Controller;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import models.dao.BorrowRecordDAO;
import models.dao.UserDAO;
import models.data.DatabaseConnection;
import models.entities.BorrowRecord;
import models.entities.BorrowedBookInfo;
import models.entities.User;
import models.entities.UserBorrowJoinInfo;
import models.services.BorrowRecordService;
import models.viewmodel.UserBorrowView;
import utils.SceneController;

import java.sql.Connection;
import java.util.*;

public class LibrarianToBorrowController {


    @FXML
    private TableView<UserBorrowView> tableView;

    @FXML
    private TableColumn<UserBorrowView, String> usernameColumn;

    @FXML
    private TableColumn<UserBorrowView, String> borrowedColumn;

    @FXML
    private TableColumn<UserBorrowView, String> returnedColumn;

    @FXML
    private TableColumn<UserBorrowView, String> dueDateColumn;


    /**
     * Phương thức khởi tạo controller khi giao diện được tải.
     * Thiết lập liên kết cột và khởi động tải dữ liệu người dùng cùng lịch sử mượn sách.
     */
    @FXML
    public void initialize() {
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        borrowedColumn.setCellValueFactory(new PropertyValueFactory<>("borrowedBooks"));
        returnedColumn.setCellValueFactory(new PropertyValueFactory<>("returnedBooks"));
        dueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        loadUserBorrowData();
    }

    /**
     * Tải dữ liệu người dùng và thông tin sách đã mượn hoặc đã trả từ cơ sở dữ liệu.
     * Dữ liệu được tải thông qua một Task bất đồng bộ để tránh chặn luồng giao diện.
     */
    private void loadUserBorrowData() {
        Task<ObservableList<UserBorrowView>> task = new Task<>() {
            @Override
            protected ObservableList<UserBorrowView> call() throws Exception {
                ObservableList<UserBorrowView> viewList = FXCollections.observableArrayList();

                try (Connection conn = DatabaseConnection.getConnection()) {
                    BorrowRecordDAO dao = new BorrowRecordDAO();
                    List<UserBorrowJoinInfo> allRecords = dao.getAllUserBorrowJoinInfo(conn);
                    BorrowRecordService borrowRecordService = new BorrowRecordService();

                    Map<String, List<UserBorrowJoinInfo>> groupedByUser = new LinkedHashMap<>();
                    for (UserBorrowJoinInfo info : allRecords) {
                        groupedByUser
                                .computeIfAbsent(info.getUsername(), k -> new ArrayList<>())
                                .add(info);
                    }

                    for (Map.Entry<String, List<UserBorrowJoinInfo>> entry : groupedByUser.entrySet()) {
                        String username = entry.getKey();
                        List<UserBorrowJoinInfo> records = entry.getValue();

                        List<String> borrowed = new ArrayList<>();
                        List<String> returned = new ArrayList<>();
                        List<String> dueDates = new ArrayList<>();

                        for (UserBorrowJoinInfo record : records) {
                            String title = record.getBookTitle();
                            Date returnDate = record.getReturnDate();

                            if (returnDate == null) {
                                borrowed.add(title);
                                // Tạo BorrowRecord để truyền vào service
                                BorrowRecord borrow = new BorrowRecord(
                                        record.getUserId(),
                                        record.getIsbn(),
                                        record.getBorrowDate(),
                                        null,
                                        ""
                                );
                                dueDates.add(borrowRecordService.getRemainingDays(borrow));
                            } else {
                                returned.add(title);
                            }
                        }

                        if (borrowed.isEmpty()) {
                            borrowed.add("chưa có cuốn sách nào");
                            dueDates.add("");
                        }
                        if (returned.isEmpty()) {
                            returned.add("chưa có cuốn sách nào");
                        }

                        viewList.add(new UserBorrowView(
                                username,
                                String.join("\n", borrowed),
                                String.join("\n", returned),
                                String.join("\n", dueDates)
                        ));
                    }
                }

                return viewList;
            }
        };

        task.setOnSucceeded(event -> tableView.setItems(task.getValue()));

        task.setOnFailed(event -> {
            System.err.println("Lỗi khi tải dữ liệu người dùng và mượn sách:");
            task.getException().printStackTrace();
        });
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }
}
