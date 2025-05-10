package Controller;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import models.dao.BorrowRecordDAO;
import models.data.DatabaseConnection;
import models.entities.BorrowedBookInfo;
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
        // Chỉ cần BorrowRecordDAO, không cần DocumentDAO vì đã tích hợp trong DAO
        this.borrowHistoryService = new BorrowHistoryService(new BorrowRecordDAO());
    }

    @FXML
    public void initialize() {
        // Cài đặt hiển thị tên sách
        bookInfoColumn.setCellValueFactory(new PropertyValueFactory<>("display"));

        // Xử lý chuyển scene
        Truyendatra1.setOnMouseClicked(event ->
                SceneController.getInstance().switchCenterContent("/fxml/ProfileSceneBorrow2.fxml")
        );

        Thongtinchung1.setOnMouseClicked(event ->
                SceneController.getInstance().switchCenterContent("/fxml/ProfileScene.fxml")
        );

        // Load dữ liệu sách đang mượn
        loadBorrowedBooks();

        // Load tên người dùng
        Task<User> loadUserTask = new Task<>() {
            @Override
            protected User call() {
                return SessionManager.getCurrentUser();
            }
        };

        loadUserTask.setOnSucceeded(event -> {
            User user = loadUserTask.getValue();
            if (user != null) {
                nameLabel1.setText(user.getUsername());
            }
        });

        new Thread(loadUserTask).start();
    }

    private void loadBorrowedBooks() {
        Task<List<BorrowedBookInfo>> task = new Task<>() {
            @Override
            protected List<BorrowedBookInfo> call() throws SQLException {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    User user = SessionManager.getCurrentUser();
                    if (user == null) return List.of();
                    return borrowHistoryService.getUnreturnedBookInfo(conn, user.getId());
                }
            }
        };

        task.setOnSucceeded(event -> {
            borrowedBooksTable.getItems().clear();
            for (BorrowedBookInfo info : task.getValue()) {
                String title = info.getDocument().getTitle();
                String isbn = info.getDocument().getIsbn();
                borrowedBooksTable.getItems().add(new BookBorrowedView(title, isbn));
            }
        });

        task.setOnFailed(event -> {
            System.err.println("Lỗi khi tải danh sách sách chưa trả:");
            task.getException().printStackTrace();
        });

        new Thread(task).start();
    }

    public void Exit() {
        SceneController.getInstance().switchCenterContent("/fxml/HomePageScene.fxml");
    }
}
