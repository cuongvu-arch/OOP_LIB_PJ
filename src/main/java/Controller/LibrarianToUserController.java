package Controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import models.dao.BorrowRecordDAO;
import models.dao.UserDAO;  // Import UserDAO
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
        // Thiết lập các cột cho TableView
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        borrowedColumn.setCellValueFactory(new PropertyValueFactory<>("borrowedBooks"));
        returnedColumn.setCellValueFactory(new PropertyValueFactory<>("returnedBooks"));

        ObservableList<UserBorrowView> viewList = FXCollections.observableArrayList();

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Lấy tất cả người dùng từ cơ sở dữ liệu thông qua UserDAO
            List<User> userList = UserDAO.getAllUser(conn);  // Gọi phương thức getAllUsers từ UserDAO

            for (User user : userList) {
                // Lấy các bản ghi mượn của người dùng từ Database
                List<BorrowRecord> records = new BorrowRecordDAO().getByUserId(conn, user.getId());

                // Danh sách các sách đã mượn và đã trả
                List<String> borrowed = new ArrayList<>();
                List<String> returned = new ArrayList<>();

                // Kiểm tra tất cả các bản ghi mượn của người dùng
                if (records.isEmpty()) {
                    // Nếu không có bản ghi mượn, đặt giá trị là "0"
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

                // Thêm đối tượng UserBorrowView vào danh sách
                viewList.add(new UserBorrowView(
                        user.getUsername(),
                        String.join("\n", borrowed),
                        String.join("\n", returned)
                ));
            }

            // Đặt dữ liệu vào TableView
            tableView.setItems(viewList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
