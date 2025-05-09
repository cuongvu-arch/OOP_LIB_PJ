package Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import models.dao.BorrowRecordDAO;
import models.dao.DocumentDAO;
import models.data.DatabaseConnection;
import models.entities.BorrowRecord;
import models.entities.Document;
import models.entities.User;
import utils.SessionManager;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class FollowScreenController {

    @FXML
    private FlowPane borrowedBooksPane;

    public void initialize() {
        loadBorrowedBooksFromSession();
    }

    private void loadBorrowedBooksFromSession() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            System.out.println("Không có người dùng đang đăng nhập.");
            return;
        }

        BorrowRecordDAO borrowRecordDAO = new BorrowRecordDAO();
        Connection conn = DatabaseConnection.getConnection();
        try {
            List<BorrowRecord> borrowedBooks = borrowRecordDAO.getByUserId(conn, currentUser.getId());
            for (BorrowRecord record : borrowedBooks) {
                // Nếu sách đã trả rồi thì bỏ qua
                if (record.getReturnDate() != null) continue;
                addBorrowedBook(record);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addBorrowedBook(BorrowRecord record) {
        try {
            Document document = getDocumentByIsbn(record.getIsbn());
            if (document != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/BookCard.fxml"));
                VBox bookCard = loader.load();

                BookCardController controller = loader.getController();
                controller.setBookInfo(document.getTitle(), document.getThumbnailUrl(), () -> {
                    try {
                        BorrowRecordDAO dao = new BorrowRecordDAO();
                        Connection conn = DatabaseConnection.getConnection();
                        dao.markAsReturned(conn, record.getUserId(), record.getIsbn());

                        borrowedBooksPane.getChildren().remove(bookCard);
                        System.out.println("Đã trả sách: " + document.getTitle());
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });

                borrowedBooksPane.getChildren().add(bookCard);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Document getDocumentByIsbn(String isbn) {
        DocumentDAO documentDAO = new DocumentDAO();
        try {
            return documentDAO.getBookByIsbn(isbn);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
