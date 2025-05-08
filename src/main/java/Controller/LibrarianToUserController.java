package Controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import models.dao.BorrowRecordDAO;
import models.data.DatabaseConnection;
import models.entities.BorrowRecord;
import models.entities.User;
import models.viewmodel.UserBorrowView;

import java.sql.Connection;
import java.util.*;

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
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        borrowedColumn.setCellValueFactory(new PropertyValueFactory<>("borrowedBooks"));
        returnedColumn.setCellValueFactory(new PropertyValueFactory<>("returnedBooks"));

        try (Connection conn = DatabaseConnection.getConnection()) {
            List<User> userList = getAllUsers();
            ObservableList<UserBorrowView> viewList = FXCollections.observableArrayList();

            for (User user : userList) {
                List<BorrowRecord> records = new BorrowRecordDAO().getByUserId(conn, user.getId());

                List<String> borrowed = new ArrayList<>();
                List<String> returned = new ArrayList<>();

                for (BorrowRecord record : records) {
                    if (record.getReturnDate() == null) {
                        borrowed.add(record.getIsbn());
                    } else {
                        returned.add(record.getIsbn());
                    }
                }

                viewList.add(new UserBorrowView(
                        user.getUsername(),
                        String.join(", ", borrowed),
                        String.join(", ", returned)
                ));
            }

            tableView.setItems(viewList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<User> getAllUsers() {
        return List.of(
                new User(1, "alice", "pass123", "alice@example.com", "0123456789", "user"),
                new User(2, "bob", "bobpass", "bob@example.com", "0987654321", "user"),
                new User(3, "charlie", "charliepw", "charlie@example.com", "0112233445", "admin")
        );
    }
}
