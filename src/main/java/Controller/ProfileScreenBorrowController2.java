package Controller;

import javafx.application.Platform;
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

public class ProfileScreenBorrowController2 {

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

    private final BorrowHistoryService borrowHistoryService;

    public ProfileScreenBorrowController2() {
        // Khởi tạo dịch vụ với DAO đã được inject hoặc bạn có thể tạo mới
        this.borrowHistoryService = new BorrowHistoryService(new DocumentDAO(), new BorrowRecordDAO());
    }

    @FXML
    public void initialize() {
        // Khởi tạo và load user
        Task<User> loadUserTask = new Task<>() {
            @Override
            protected User call() {
                return SessionManager.getCurrentUser();
            }
        };

        // Khi Task hoàn thành, cập nhật UI
        loadUserTask.setOnSucceeded(event -> {
            User user = loadUserTask.getValue();
            if (user != null) {
                // Hiển thị tên người dùng
                nameLabel2.setText(user.getUsername());

                // Load sách đã trả đúng theo userId
                loadReturnedBooks();
            }
        });

        // Bắt đầu chạy task ở thread phụ
        new Thread(loadUserTask).start();

        // Cấu hình cột table để hiển thị thông tin sách
        bookInfoColumn2.setCellValueFactory(cellData -> cellData.getValue().displayProperty());

        // Xử lý chuyển màn hình khi click
        Truyendangmuon2.setOnMouseClicked(event -> {
            SceneController.getInstance().switchCenterContent("/fxml/ProfileSceneBorrow1.fxml");
        });

        Thongtinchung2.setOnMouseClicked(event -> {
            SceneController.getInstance().switchCenterContent("/fxml/ProfileScene.fxml");
        });
    }

    private void loadReturnedBooks() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Lấy user hiện tại
            User user = SessionManager.getCurrentUser();
            if (user == null) return;

            int userId = user.getId();

            // Lấy danh sách sách chưa trả từ dịch vụ
            List<Document> borrowedBooks = borrowHistoryService.getReturnedBooks(conn, userId);

            // Chuyển đổi thành các đối tượng BookBorrowedView để hiển thị
            for (Document doc : borrowedBooks) {
                borrowedBooksTable2.getItems().add(new BookBorrowedView(doc.getTitle(), doc.getIsbn()));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void Exit() {
        SceneController.getInstance().switchCenterContent("/fxml/HomePageScene.fxml");
    }
}
