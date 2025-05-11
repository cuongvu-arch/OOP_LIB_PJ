package Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import models.dao.BorrowRecordDAO;
import models.dao.DocumentDAO;
import models.data.DatabaseConnection;
import models.entities.BorrowRecord;
import models.entities.BorrowedBookInfo;
import models.entities.Document;
import models.entities.User;
import utils.SessionManager;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javafx.concurrent.Task;

/**
 * Controller cho màn hình theo dõi các sách đang được mượn.
 * Hiển thị danh sách các sách mà người dùng hiện tại đang mượn nhưng chưa trả.
 */
public class FollowScreenController {

    @FXML
    private FlowPane borrowedBooksPane;


    /**
     * Phương thức khởi tạo controller.
     * Được gọi tự động sau khi FXML được tải, và bắt đầu tiến trình tải danh sách sách đang mượn.
     */
    public void initialize() {
        loadBorrowedBooksInBackground();
    }

    /**
     * Tải danh sách sách đang được mượn trong một thread nền (background thread)
     * để tránh làm treo giao diện người dùng.
     */
    private void loadBorrowedBooksInBackground() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            System.out.println("Không có người dùng đang đăng nhập.");
            return;
        }

        Task<List<BorrowedBookInfo>> loadTask = new Task<>() {
            @Override
            protected List<BorrowedBookInfo> call() throws Exception {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    BorrowRecordDAO borrowRecordDAO = new BorrowRecordDAO();
                    return borrowRecordDAO.getBorrowedBooksWithInfoByUserId(conn, currentUser.getId());
                }
            }
        };

        loadTask.setOnSucceeded(workerStateEvent -> {
            List<BorrowedBookInfo> infos = loadTask.getValue();
            for (BorrowedBookInfo info : infos) {
                if (info.getBorrowRecord().getReturnDate() == null) {
                    addBorrowedBook(info);
                }
            }
        });

        loadTask.setOnFailed(e -> {
            System.err.println("Lỗi khi tải danh sách mượn sách:");
            loadTask.getException().printStackTrace();
        });

        Thread thread = new Thread(loadTask);
        thread.setDaemon(true);
        thread.start();
    }


    /**
     * Tạo và thêm một card sách vào giao diện nếu sách đó chưa được trả.
     *
     * @param info Thông tin kết hợp giữa sách và bản ghi mượn sách.
     */
    private void addBorrowedBook(BorrowedBookInfo info) {
        Document document = info.getDocument();
        BorrowRecord record = info.getBorrowRecord();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/BookCard.fxml"));
            VBox bookCard = loader.load();

            BookCardController controller = loader.getController();
            controller.setBookInfo(document.getTitle(), document.getThumbnailUrl(), () -> {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    BorrowRecordDAO dao = new BorrowRecordDAO();
                    dao.markAsReturned(conn, record.getUserId(), record.getIsbn());

                    borrowedBooksPane.getChildren().remove(bookCard);
                    System.out.println("Đã trả sách: " + document.getTitle());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });

            borrowedBooksPane.getChildren().add(bookCard);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
