package Controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import models.dao.BorrowRecordDAO;
import models.entities.BorrowRecord;
import models.entities.Document;
import models.entities.User;
import utils.BookImageLoader;
import javafx.scene.image.Image;

import javafx.event.ActionEvent;
import java.net.URL;
import models.data.DatabaseConnection;
import utils.SessionManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;



public class BookDetailController {

    @FXML private ImageView bookCoverImageView;
    @FXML private Text bookTitleText;
    @FXML private Text bookAuthorsText;
    @FXML private Text publishDateText;
    @FXML private Text publisherText;
    @FXML private Text isbnText;
    @FXML private Text languageText;
    @FXML private TextArea descriptionTextArea;
    @FXML private Button closeButton;
    @FXML
    private VBox commentsVBox;

    @FXML
    private TextArea newCommentTextArea;

    @FXML
    private ScrollPane commentsScrollPane;

    private Document currentBook;

    public void initialize() {
    }

    @FXML
    private void handleBorrowButtonClick(ActionEvent event) {
        Document selectedDoc = currentBook;  // Sử dụng sách đang xem chi tiết
        if (selectedDoc == null) {
            System.out.println("Chưa chọn sách nào.");
            return;
        }

        User currentUser = SessionManager.getCurrentUser();  // Lấy người dùng hiện tại
        if (currentUser == null) {
            System.out.println("Bạn cần đăng nhập để mượn sách.");
            return;
        }

        // Kết nối cơ sở dữ liệu và thêm thông tin mượn sách
        try (Connection conn = DatabaseConnection.getConnection()) {
            BorrowRecordDAO borrowRecordDAO = new BorrowRecordDAO();
            BorrowRecord borrowRecord = new BorrowRecord(currentUser.getId(), selectedDoc.getIsbn(), new java.sql.Date(new Date().getTime()), null);  // Ngày mượn là ngày hiện tại, chưa có ngày trả

            if (borrowRecordDAO.isBorrowed(conn, currentUser.getId(), selectedDoc.getIsbn())) {
                // Tạo thông báo khi đã mượn sách rồi
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Thông báo");
                alert.setHeaderText(null);
                alert.setContentText("Bạn đã mượn sách này rồi.");
                alert.showAndWait();
            } else {
                borrowRecordDAO.add(conn, borrowRecord);  // Thêm thông tin vào cơ sở dữ liệu
                // Tạo thông báo khi mượn sách thành công
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Thông báo");
                alert.setHeaderText(null);
                alert.setContentText("Đã mượn sách: " + selectedDoc.getTitle());
                alert.showAndWait();

            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi mượn sách: " + e.getMessage());
        }
    }



    public void setBookData(Document book) {
        this.currentBook = book;
        if (book == null) {
            System.err.println("Book data is null in BookDetailController.");
            bookTitleText.setText("Không có thông tin sách");
            return;
        }

        bookTitleText.setText(book.getTitle() != null ? book.getTitle() : "N/A");

        if (book.getAuthors() != null && book.getAuthors().length > 0) {
            bookAuthorsText.setText("bởi " + String.join(", ", book.getAuthors()));
        } else {
            bookAuthorsText.setText("bởi Tác giả không xác định");
        }

        publishDateText.setText(book.getPublishedDate() != null ? book.getPublishedDate() : "N/A");
        publisherText.setText(book.getPublisher() != null ? book.getPublisher() : "N/A");
        isbnText.setText(book.getIsbn() != null ? book.getIsbn() : "N/A");
        descriptionTextArea.setText(book.getDescription() != null && !book.getDescription().isEmpty() ? book.getDescription() : "Không có mô tả.");
        languageText.setText("Tiếng Anh");

        if (book.getThumbnailUrl() != null && !book.getThumbnailUrl().isEmpty()) {
            BookImageLoader.loadImage(book.getThumbnailUrl(), bookCoverImageView);
        } else {
            try {
                String placeholderPath = "/image/img.png";
                URL resourceUrl = getClass().getResource(placeholderPath);
                if (resourceUrl != null) {
                    bookCoverImageView.setImage(new Image(resourceUrl.toExternalForm()));
                } else {
                    System.err.println("Không tìm thấy ảnh placeholder: " + placeholderPath);
                }
            } catch (Exception e) {
                System.err.println("Lỗi tải ảnh placeholder: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleCloseButtonClick() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
    @FXML
    private void handleSubmitComment(ActionEvent event) {
        String commentText = newCommentTextArea.getText().trim();
        if (!commentText.isEmpty()) {
            addCommentToUI("Tên người dùng", commentText); // Thay bằng tên thực tế
            newCommentTextArea.clear();
        }
    }

    private void addCommentToUI(String username, String comment) {
        VBox commentBox = new VBox(5);
        commentBox.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 10; -fx-border-radius: 5; -fx-background-radius: 5;");

        Label userLabel = new Label(username);
        userLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");

        Text commentText = new Text(comment);
        commentText.setWrappingWidth(commentsScrollPane.getWidth() - 30);

        commentBox.getChildren().addAll(userLabel, commentText);
        commentsVBox.getChildren().add(0, commentBox); // Thêm lên đầu danh sách


        Platform.runLater(() -> {
            commentsScrollPane.setVvalue(0);
        });
    }
}
