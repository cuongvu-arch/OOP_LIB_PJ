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
import models.services.BorrowRecordService;
import models.viewmodel.BookBorrowedView;
import utils.SceneController;
import utils.SessionManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class ProfileScreenBorrowController2 {

    private final BorrowRecordService borrowRecordService;
    @FXML
    private TableView<BookBorrowedView> borrowedBooksTable2;
    @FXML
    private TableColumn<BookBorrowedView, String> bookInfoColumn2;
    @FXML
    private Label Truyendangmuon2;
    @FXML
    private Label Thongtinchung2;
    @FXML
    private Label nameLabel2;

    /**
     * Constructor khởi tạo service để truy xuất dữ liệu mượn trả.
     */
    public ProfileScreenBorrowController2() {
        this.borrowRecordService = new BorrowRecordService();
    }

    /**
     * Phương thức khởi tạo sau khi FXML được load.
     * Thiết lập sự kiện cho nhãn, hiển thị tên người dùng,
     * và gọi hàm load danh sách sách đã trả.
     */
    @FXML
    public void initialize() {
        // Thiết lập cột trong bảng
        bookInfoColumn2.setCellValueFactory(new PropertyValueFactory<>("display"));

        // Chuyển trang
        Truyendangmuon2.setOnMouseClicked(event ->
                SceneController.getInstance().switchCenterContent("/fxml/ProfileSceneBorrow1.fxml")
        );

        Thongtinchung2.setOnMouseClicked(event ->
                SceneController.getInstance().switchCenterContent("/fxml/ProfileScene.fxml")
        );

        // Load user + dữ liệu sách đã trả
        Task<User> loadUserTask = new Task<>() {
            @Override
            protected User call() {
                return SessionManager.getCurrentUser();
            }
        };

        loadUserTask.setOnSucceeded(event -> {
            User user = loadUserTask.getValue();
            if (user != null) {
                nameLabel2.setText(user.getUsername());
                loadReturnedBooks(user.getId());
            }
        });

        new Thread(loadUserTask).start();
    }

    /**
     * Tải danh sách sách đã trả từ cơ sở dữ liệu và hiển thị trong bảng.
     *
     * @param userId ID của người dùng cần truy xuất thông tin
     */
    private void loadReturnedBooks(int userId) {
        Task<List<BorrowedBookInfo>> task = new Task<>() {
            @Override
            protected List<BorrowedBookInfo> call() throws SQLException {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    return borrowRecordService.getReturnedBookInfo(conn, userId);
                }
            }
        };

        task.setOnSucceeded(event -> {
            borrowedBooksTable2.getItems().clear();
            for (BorrowedBookInfo info : task.getValue()) {
                String title = info.getDocument().getTitle();
                String isbn = info.getDocument().getIsbn();
                borrowedBooksTable2.getItems().add(new BookBorrowedView(title, isbn));
            }
        });

        task.setOnFailed(event -> {
            System.err.println("Lỗi khi tải danh sách sách đã trả:");
            task.getException().printStackTrace();
        });

        new Thread(task).start();
    }

    /**
     * Xử lý hành động khi người dùng nhấn vào nút "Thoát".
     * Điều hướng về giao diện trang chủ.
     */
    public void Exit() {
        SceneController.getInstance().switchCenterContent("/fxml/HomePageScene.fxml");
    }
}
