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

public class ProfileScreenBorrowController1 {

    private final BorrowRecordService borrowRecordService;
    @FXML
    private Label Truyendangmuon;
    @FXML
    private TableView<BookBorrowedView> borrowedBooksTable;
    @FXML
    private TableColumn<BookBorrowedView, String> bookInfoColumn;
    @FXML
    private Label Truyendatra;
    @FXML
    private Label Thongtinchung;
    @FXML
    private Label nameLabel;



    /**
     * Khởi tạo controller. Khởi tạo BorrowRecordService.
     */
    public ProfileScreenBorrowController1() {
        // Chỉ cần BorrowRecordDAO, không cần DocumentDAO vì đã tích hợp trong DAO
        this.borrowRecordService = new BorrowRecordService();
    }

    /**
     * Phương thức khởi tạo sau khi các thành phần @FXML được inject.
     * - Thiết lập hiển thị cột thông tin sách.
     * - Thiết lập xử lý sự kiện chuyển scene.
     * - Tải danh sách sách đang mượn và tên người dùng bất đồng bộ.
     */
    @FXML
    public void initialize() {
        // Cài đặt hiển thị tên sách
        bookInfoColumn.setCellValueFactory(new PropertyValueFactory<>("display"));

        // Xử lý chuyển scene
        Truyendatra.setOnMouseClicked(event ->
                SceneController.getInstance().switchCenterContent("/fxml/ProfileSceneBorrow2.fxml")
        );

        Thongtinchung.setOnMouseClicked(event ->
                SceneController.getInstance().switchCenterContent("/fxml/ProfileScene.fxml")
        );

        Truyendangmuon.setOnMouseClicked(event -> SceneController.getInstance().switchToScene("/fxml/ProfileSceneBorrow1.fxml"));

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
                nameLabel.setText(user.getUsername());
            }
        });

        new Thread(loadUserTask).start();
    }

    /**
     * Tải danh sách sách chưa được trả từ cơ sở dữ liệu và hiển thị lên bảng.
     * Dữ liệu được lấy qua dịch vụ BorrowRecordService sử dụng connection JDBC.
     */
    private void loadBorrowedBooks() {
        Task<List<BorrowedBookInfo>> task = new Task<>() {
            @Override
            protected List<BorrowedBookInfo> call() throws SQLException {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    User user = SessionManager.getCurrentUser();
                    if (user == null) return List.of();
                    return borrowRecordService.getUnreturnedBookInfo(conn, user.getId());
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

    /**
     * Xử lý khi người dùng nhấn nút "Thoát" để quay về trang chủ.
     */
    public void Exit() {
        SceneController.getInstance().switchCenterContent("/fxml/HomePageScene.fxml");
    }
}
