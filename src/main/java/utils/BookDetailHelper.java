package utils;

import Controller.BookDetailController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.dao.DocumentDAO;
import models.entities.Document;

import java.io.IOException;
import java.sql.SQLException;

// utils/BookDetailHelper.java
public class BookDetailHelper {
    public static void openBookDetailWindow(Document book) {
        System.out.println("openBookDetailWindow() được gọi với book: " + book);
        try {
            // ✅ Tải lại thông tin sách đầy đủ từ CSDL
            DocumentDAO documentDAO = new DocumentDAO();
            Document fullBook = documentDAO.getBookByIsbn(book.getIsbn());

            if (fullBook == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Không tìm thấy sách với ISBN: " + book.getIsbn());
                alert.showAndWait();
                return;
            }

            FXMLLoader loader = new FXMLLoader(BookDetailHelper.class.getResource("/fxml/BookDetailScreen.fxml"));
            Parent root = loader.load();

            BookDetailController controller = loader.getController();
            controller.setBookData(fullBook); // ✅ Truyền bản đầy đủ

            Stage detailStage = new Stage();
            detailStage.setTitle("Chi tiết sách: " + (fullBook.getTitle() != null ? fullBook.getTitle() : "Không có tiêu đề"));
            detailStage.setScene(new Scene(root));
            detailStage.initModality(Modality.APPLICATION_MODAL);
            detailStage.setMaximized(true);
            detailStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Không thể mở trang chi tiết sách: " + e.getMessage());
            alert.showAndWait();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}

