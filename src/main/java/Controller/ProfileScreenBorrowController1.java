package Controller;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import models.dao.BorrowRecordDAO;
import models.dao.DocumentDAO;
import models.data.DatabaseConnection;
import models.entities.Document;
import models.entities.User;
import models.services.BorrowHistoryService;
import models.viewmodel.BookBorrowedView;
import utils.SceneController;
import utils.SessionManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class ProfileScreenBorrowController1 {

    @FXML
    private TableView<BookBorrowedView> borrowedBooksTable;

    @FXML
    private TableColumn<BookBorrowedView, String> bookInfoColumn;

    @FXML
    private Label Truyendatra1;

    @FXML
    private Label Thongtinchung1;

    @FXML
    private Label nameLabel1;

    private final BorrowHistoryService borrowHistoryService;

    public ProfileScreenBorrowController1() {
        // Khởi tạo dịch vụ với DAO đã được inject hoặc bạn có thể tạo mới
        this.borrowHistoryService = new BorrowHistoryService(new DocumentDAO(), new BorrowRecordDAO());
    }

    @FXML
    public void initialize() {
        Task<User> loadUserTask = new Task<>() {
            @Override
            protected User call() {
                return SessionManager.getCurrentUser();
            }
        };

        // Cấu hình cột trong TableView
        bookInfoColumn.setCellValueFactory(new PropertyValueFactory<>("display"));

        Truyendatra1.setOnMouseClicked(event -> {
            SceneController.getInstance().switchCenterContent("/fxml/ProfileSceneBorrow2.fxml");
        });

        Thongtinchung1.setOnMouseClicked(event -> {
            SceneController.getInstance().switchCenterContent("/fxml/ProfileScene.fxml");
        });

        // Load sách đang mượn
        loadBorrowedBooks();

        // Hiển thị tên người dùng khi load xong
        loadUserTask.setOnSucceeded(event -> {
            User user = loadUserTask.getValue();
            if (user != null && nameLabel1 != null) {
                nameLabel1.setText(user.getUsername());
            }
        });

        new Thread(loadUserTask).start();
    }


    private void loadBorrowedBooks() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Lấy user hiện tại
            User user = SessionManager.getCurrentUser();
            if (user == null) return;

            int userId = user.getId();

            // Lấy danh sách sách chưa trả từ dịch vụ
            List<Document> borrowedBooks = borrowHistoryService.getUnreturnedBooks(conn, userId);

            // Chuyển đổi thành các đối tượng BookBorrowedView để hiển thị
            for (Document doc : borrowedBooks) {
                borrowedBooksTable.getItems().add(new BookBorrowedView(doc.getTitle(), doc.getIsbn()));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void Exit() {
        SceneController.getInstance().switchToScene("/fxml/BaseLayout.fxml");
    }
}
