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

public class FollowScreenController {

    @FXML
    private FlowPane borrowedBooksPane;

    public void initialize() {
        loadBorrowedBooksInBackground();
    }

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
