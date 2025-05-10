package Controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import models.dao.BorrowRecordDAO;
import models.dao.UserDAO;
import models.data.DatabaseConnection;
import models.entities.BorrowRecord;
import models.entities.User;
import models.viewmodel.UserBorrowView;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class LibrarianToUserController {

    @FXML
    private TableView<UserBorrowView> tableView;

    @FXML
    private TableColumn<UserBorrowView, String> usernameColumn;

    @FXML
    private TableColumn<UserBorrowView, String> borrowedColumn;

    @FXML
    private TableColumn<UserBorrowView, String> returnedColumn;

    @FXML
    public void initialize() {
        // Thiết lập các cột cho TableView
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        borrowedColumn.setCellValueFactory(new PropertyValueFactory<>("borrowedBooks"));
        returnedColumn.setCellValueFactory(new PropertyValueFactory<>("returnedBooks"));

        loadUserBorrowData();
    }

    private void loadUserBorrowData() {
        Task<ObservableList<UserBorrowView>> task = new Task<>() {
            @Override
            protected ObservableList<UserBorrowView> call() throws Exception {
                ObservableList<UserBorrowView> viewList = FXCollections.observableArrayList();

                try (Connection conn = DatabaseConnection.getConnection()) {
                    List<User> userList = UserDAO.getAllUser(conn);
                    BorrowRecordDAO borrowRecordDAO = new BorrowRecordDAO();

                    for (User user : userList) {
                        List<BorrowRecord> records = borrowRecordDAO.getByUserId(conn, user.getId());

                        List<String> borrowed = new ArrayList<>();
                        List<String> returned = new ArrayList<>();

                        if (records.isEmpty()) {
                            borrowed.add("chưa có cuốn sách nào");
                            returned.add("chưa có cuốn sách nào");
                        } else {
                            for (BorrowRecord record : records) {
                                if (record.getReturnDate() == null) {
                                    borrowed.add(record.getIsbn());
                                } else {
                                    returned.add(record.getIsbn());
                                }
                            }
                        }

                        viewList.add(new UserBorrowView(
                                user.getUsername(),
                                String.join("\n", borrowed),
                                String.join("\n", returned)
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
