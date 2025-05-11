package Controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import models.dao.BorrowRecordDAO;
import models.dao.DocumentDAO;
import models.dao.ReviewDAO;
import models.entities.BorrowRecord;
import models.entities.Review;
import models.entities.Document;
import models.entities.User;
import models.services.DocumentService;
import utils.AlertUtils;
import utils.BookImageLoader;
import javafx.scene.image.Image;

import javafx.event.ActionEvent;

import java.awt.*;
import java.io.File;
import java.net.URL;

import models.data.DatabaseConnection;
import utils.SessionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;


public class BookDetailController {
    @FXML
    private Button regenerateQRButton;
    @FXML
    private Button submitRatingButton;
    @FXML
    private ImageView qrCodeImageView;
    @FXML
    private ImageView bookCoverImageView;
    @FXML
    private Text bookTitleText;
    @FXML
    private Text bookAuthorsText;
    @FXML
    private Text publishDateText;
    @FXML
    private Text publisherText;
    @FXML
    private Text isbnText;
    @FXML
    private Text languageText;
    @FXML
    private Text avgRatingText;
    @FXML
    private TextArea descriptionTextArea;
    @FXML
    private Button closeButton;
    @FXML
    private VBox commentsVBox;
    @FXML
    private TextArea newCommentTextArea;
    @FXML
    private ScrollPane commentsScrollPane;
    @FXML
    private ChoiceBox<Integer> ratingChoiceBox;
    @FXML
    private Button borrowButton;

    private Document currentBook;
    private DocumentService documentService;

    public void initialize() throws Exception {
        ReviewDAO.loadReviewData();
        ratingChoiceBox.getItems().addAll(1, 2, 3, 4, 5);
        ratingChoiceBox.setValue(5);
        try {
            documentService = new DocumentService();
        } catch (Exception e) {
            System.err.println("Error initializing DocumentService: " + e.getMessage());
        }
        User currentUser = SessionManager.getCurrentUser();
        regenerateQRButton.setDisable(currentUser == null || !"admin".equalsIgnoreCase(currentUser.getRole()));
        if(SessionManager.getCurrentUser().getRole() != "admin") {
            regenerateQRButton.setVisible(false);
        }
        qrCodeImageView.setVisible(true); // Đảm bảo ImageView luôn hiển thị
    }

    @FXML
    private void handleSubmitComment(ActionEvent event) {
        String commentText = newCommentTextArea.getText().trim();
        if (!commentText.isEmpty() && currentBook != null) {
            User currentUser = SessionManager.getCurrentUser();
            if (currentUser == null) {
                System.out.println("Người dùng chưa đăng nhập.");
                return;
            }

            Review review = new Review(
                    currentUser.getId(),
                    currentBook.getIsbn(),
                    0,  // Không có rating trong comment
                    commentText,
                    LocalDateTime.now()
            );

            ReviewDAO.addReview(review); // Lưu vào CSDL

            addCommentToUI(currentUser.getUsername(), commentText); // Cập nhật giao diện
            newCommentTextArea.clear();
        }
    }

    @FXML
    private void handleSubmitRating(ActionEvent event) {
        Integer rating = ratingChoiceBox.getValue();
        if (rating != null && currentBook != null) {
            User currentUser = SessionManager.getCurrentUser();
            if (currentUser == null) {
                System.out.println("Người dùng chưa đăng nhập.");
                return;
            }

            String commentText = "Đánh giá: " + rating.toString(); // Nếu cần, có thể thêm comment cho rating

            Review review = new Review(
                    currentUser.getId(),
                    currentBook.getIsbn(),
                    rating,  // Sử dụng rating nhập từ ChoiceBox
                    commentText,
                    LocalDateTime.now()
            );

            ReviewDAO.addReview(review); // Lưu vào CSDL

            addCommentToUI(currentUser.getUsername(), "Đánh giá: " + rating); // Cập nhật giao diện với rating
        }
    }


    private void saveReviewToDatabase(int rating, String comment) {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            System.out.println("Bạn cần đăng nhập để đánh giá.");
            return;
        }

        // Tạo đối tượng Review và lưu vào cơ sở dữ liệu
        Review review = new Review(currentUser.getId(), currentBook.getIsbn(), rating, comment, LocalDateTime.now());
        ReviewDAO.addReview(review);  // Lưu review vào cơ sở dữ liệu
    }

    @FXML
    private void handleBorrowButtonClick(ActionEvent event) {
        Document selectedDoc = currentBook;

        // Sử dụng sách đang xem chi tiết
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
            BorrowRecord borrowRecord = new BorrowRecord(currentUser.getId(), selectedDoc.getIsbn(), new java.sql.Date(new Date().getTime()), null, "Đang mượn");  // Ngày mượn là ngày hiện tại, chưa có ngày trả

            if (borrowRecordDAO.isBorrowed(conn, currentUser.getId(), selectedDoc.getIsbn())) {
                // Tạo thông báo khi đã mượn sách rồi
                AlertUtils.showAlert("Thông báo", "Bạn đã mượn sách này rồi", Alert.AlertType.INFORMATION);
            } else {
                borrowRecordDAO.add(conn, borrowRecord);  // Thêm thông tin vào cơ sở dữ liệu
                // Tạo thông báo khi mượn sách thành công
                AlertUtils.showAlert("Thông báo", "Đã mượn sách: " + selectedDoc.getTitle(), Alert.AlertType.INFORMATION);
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

        User currentUser = SessionManager.getCurrentUser();
        // Kiểm tra và tạo mã QR nếu cần (chỉ cho admin)
        if ((book.getQrCodePath() == null || book.getQrCodePath().isEmpty()) && currentUser != null && "admin".equalsIgnoreCase(currentUser.getRole())) {
            try {
                String newQrCodePath = documentService.regenerateQRCode(book, currentUser);
                book.setQrCodePath(newQrCodePath);
                System.out.println("Generated new QR code for ISBN: " + book.getIsbn() + " at: " + newQrCodePath);
            } catch (Exception e) {
                System.err.println("Error generating QR code for ISBN: " + book.getIsbn() + ": " + e.getMessage());
            }
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

        // Load book cover image
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

        // Load QR code image
        loadQRCodeImage();

        loadReviewsForCurrentBook();
        updateAvgRating();

        if (book.getQuantity() == 0) {
            borrowButton.setDisable(true);
        }
    }

    private void updateAvgRating() {
        if (currentBook == null) return;  // Nếu không có sách hiện tại

        String isbn = currentBook.getIsbn();  // Lấy ISBN của sách hiện tại
        double avgRating = ReviewDAO.calculateAverageRating(isbn);  // Tính giá trị trung bình

        // Cập nhật giá trị trung bình vào ô avgRatingText
        if (avgRating > 0) {
            avgRatingText.setText(String.format("%.1f", avgRating));  // Hiển thị trung bình với 1 chữ số thập phân
        } else {
            avgRatingText.setText("Chưa có đánh giá");  // Nếu chưa có đánh giá
        }
    }

    private void loadReviewsForCurrentBook() {
        commentsVBox.getChildren().clear();

        if (currentBook == null) return;

        List<Review> allReviews = ReviewDAO.getAllReviewsFromMemory();
        for (Review review : allReviews) {
            if (review.getDocumentIsbn().equals(currentBook.getIsbn())) {
                String username = "Người dùng #" + review.getUserId();
                if (review.getRating() > 0) {
                    addRatingToUI(username, review.getRating());
                }
                if (review.getComment() != null && !review.getComment().isBlank()) {
                    addCommentToUI(username, review.getComment());
                }
            }
        }
    }

    @FXML
    private void handleCloseButtonClick() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
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

    private void addRatingToUI(String username, Integer rating) {
        VBox ratingBox = new VBox(5);
        ratingBox.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 10; -fx-border-radius: 5; -fx-background-radius: 5;");

        Label userLabel = new Label(username);
        userLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");

        Text ratingText = new Text("Đánh giá: " + rating);
        ratingText.setWrappingWidth(commentsScrollPane.getWidth() - 30);

        ratingBox.getChildren().addAll(userLabel, ratingText);
        commentsVBox.getChildren().add(0, ratingBox); // Thêm lên đầu danh sách
    }

    @FXML
    private void handleRegenerateQRButtonClick() {
        if (currentBook == null) {
            AlertUtils.showAlert("Lỗi", "Không có sách để tạo lại mã QR", Alert.AlertType.ERROR);
            return;
        }

        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null || !"admin".equalsIgnoreCase(currentUser.getRole())) {
            AlertUtils.showAlert("Lỗi", "Chỉ admin mới có thể tạo lại mã QR", Alert.AlertType.ERROR);
            return;
        }

        if (documentService == null) {
            AlertUtils.showAlert("Lỗi", "Không thể khởi tạo dịch vụ QR code. Vui lòng thử lại.", Alert.AlertType.ERROR);
            System.err.println("DocumentService is null");
            return;
        }

        try {
            String newQrCodePath = documentService.regenerateQRCode(currentBook, currentUser);
            currentBook.setQrCodePath(newQrCodePath);
            loadQRCodeImage();
            AlertUtils.showAlert("Thành công", "Đã tạo lại mã QR cho sách: " + currentBook.getTitle(), Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            System.err.println("Lỗi tạo lại mã QR: " + e.getMessage());
            AlertUtils.showAlert("Lỗi", "Không thể tạo lại mã QR: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadQRCodeImage() {
        if (currentBook != null && currentBook.getQrCodePath() != null && !currentBook.getQrCodePath().isEmpty()) {
            try {
                File qrFile = new File(currentBook.getQrCodePath());
                System.out.println("Checking QR code file: " + qrFile.getAbsolutePath() + ", exists: " + qrFile.exists());
                if (qrFile.exists()) {
                    qrCodeImageView.setImage(new Image(qrFile.toURI().toString()));
                } else {
                    System.err.println("QR code file not found: " + currentBook.getQrCodePath());
                    qrCodeImageView.setImage(null);
                }
            } catch (Exception e) {
                System.err.println("Lỗi tải QR code: " + e.getMessage());
                qrCodeImageView.setImage(null);
            }
        } else {
            System.err.println("QR code path is null or empty for ISBN: " + (currentBook != null ? currentBook.getIsbn() : "null"));
            qrCodeImageView.setImage(null);
        }
    }

    private void updateQrCodePathInDatabase(String isbn, String qrCodePath) {
        String sql = "UPDATE books SET qr_code_path = ? WHERE isbn = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, qrCodePath);
            stmt.setString(2, isbn);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating qr_code_path for ISBN: " + isbn + ": " + e.getMessage());
        }
    }
}
