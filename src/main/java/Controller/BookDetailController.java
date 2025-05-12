package Controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import models.dao.BorrowRecordDAO;
import models.dao.ReviewDAO;
import models.data.DatabaseConnection;
import models.entities.BorrowRecord;
import models.entities.Document;
import models.entities.Review;
import models.entities.User;
import models.services.DocumentService;
import utils.AlertUtils;
import utils.BookImageLoader;
import utils.SessionManager;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * Controller cho giao diện chi tiết sách trong ứng dụng JavaFX.
 * Cho phép người dùng xem thông tin chi tiết của một cuốn sách, đánh giá, bình luận và thực hiện hành động mượn sách.
 * Nếu là admin, người dùng còn có thể tái tạo mã QR cho sách.
 */
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

    /**
     * Khởi tạo controller, tải dữ liệu cần thiết như danh sách đánh giá và cấu hình quyền admin.
     * Thực hiện trên thread nền để tránh chặn giao diện.
     */
    public void initialize() {
        Task<Void> initTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                ReviewDAO.loadReviewData();
                try {
                    documentService = new DocumentService();
                } catch (Exception e) {
                    System.err.println("Error initializing DocumentService: " + e.getMessage());
                }
                User currentUser = SessionManager.getCurrentUser();
                boolean isAdmin = currentUser != null && "admin".equalsIgnoreCase(currentUser.getRole());

                Platform.runLater(() -> {
                    ratingChoiceBox.getItems().addAll(1, 2, 3, 4, 5);
                    ratingChoiceBox.setValue(5);
                    regenerateQRButton.setDisable(!isAdmin);
                    regenerateQRButton.setVisible(isAdmin);
                    qrCodeImageView.setVisible(true);
                });
                return null;
            }

            @Override
            protected void failed() {
                getException().printStackTrace();
                Platform.runLater(() -> AlertUtils.showAlert("Lỗi", "Không thể khởi tạo dữ liệu.", Alert.AlertType.ERROR));
            }
        };
        new Thread(initTask).start();
    }

    /**
     * Xử lý khi người dùng gửi bình luận mới.
     * Bình luận sẽ được lưu trữ thông qua ReviewDAO và hiển thị lên giao diện.
     * Yêu cầu người dùng đã đăng nhập và sách hiện tại không null.
     */
    @FXML
    private void handleSubmitComment() {
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

            Task<Void> saveCommentTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    ReviewDAO.addReview(review);
                    Platform.runLater(() -> {
                        addCommentToUI(currentUser.getUsername(), commentText);
                        newCommentTextArea.clear();
                    });
                    return null;
                }

                @Override
                protected void failed() {
                    getException().printStackTrace();
                    Platform.runLater(() -> AlertUtils.showAlert("Lỗi", "Không thể gửi bình luận.", Alert.AlertType.ERROR));
                }
            };
            new Thread(saveCommentTask).start();
        }
    }

    /**
     * Xử lý khi người dùng gửi đánh giá.
     * Đánh giá được lưu vào DAO và cập nhật lại giao diện với đánh giá mới và điểm trung bình mới.
     * Yêu cầu người dùng đã đăng nhập và sách hiện tại không null.
     */
    @FXML
    private void handleSubmitRating() {
        Integer rating = ratingChoiceBox.getValue();
        if (rating != null && currentBook != null) {
            User currentUser = SessionManager.getCurrentUser();
            if (currentUser == null) {
                System.out.println("Người dùng chưa đăng nhập.");
                return;
            }

            String commentText = "Đánh giá: " + rating.toString();

            Review review = new Review(
                    currentUser.getId(),
                    currentBook.getIsbn(),
                    rating,
                    commentText,
                    LocalDateTime.now()
            );

            Task<Void> saveRatingTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    ReviewDAO.addReview(review);
                    Platform.runLater(() -> {
                        addRatingToUI(currentUser.getUsername(), rating);
                        updateAvgRatingOnUI(); // Update average rating after submitting a new rating
                    });
                    return null;
                }

                @Override
                protected void failed() {
                    getException().printStackTrace();
                    Platform.runLater(() -> AlertUtils.showAlert("Lỗi", "Không thể gửi đánh giá.", Alert.AlertType.ERROR));
                }
            };
            new Thread(saveRatingTask).start();
        }
    }

    /**
     * Xử lý khi người dùng nhấn nút "Mượn".
     * Nếu người dùng chưa từng mượn sách này, tạo bản ghi mượn mới.
     * Nếu đã mượn rồi, hiện thông báo.
     * Tất cả thao tác với database được thực hiện ở background thread.
     */
    @FXML
    private void handleBorrowButtonClick() {
        Document selectedDoc = currentBook;
        User currentUser = SessionManager.getCurrentUser();
        if (selectedDoc == null || currentUser == null) return;

        Task<Void> borrowTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    BorrowRecordDAO borrowRecordDAO = new BorrowRecordDAO();
                    if (borrowRecordDAO.isBorrowed(conn, currentUser.getId(), selectedDoc.getIsbn())) {
                        Platform.runLater(() ->
                                AlertUtils.showAlert("Thông báo", "Bạn đã mượn sách này rồi", Alert.AlertType.INFORMATION)
                        );
                    } else {
                        BorrowRecord borrowRecord = new BorrowRecord(currentUser.getId(), selectedDoc.getIsbn(),
                                new java.sql.Date(new Date().getTime()), null, "Đang mượn");
                        borrowRecordDAO.add(conn, borrowRecord);
                        Platform.runLater(() ->
                                AlertUtils.showAlert("Thông báo", "Đã mượn sách: " + selectedDoc.getTitle(), Alert.AlertType.INFORMATION)
                        );
                    }
                }
                return null;
            }

            @Override
            protected void failed() {
                getException().printStackTrace();
                Platform.runLater(() -> AlertUtils.showAlert("Lỗi", "Không thể thực hiện mượn sách.", Alert.AlertType.ERROR));
            }
        };
        new Thread(borrowTask).start();
    }

    /**
     * Gán dữ liệu sách hiện tại và hiển thị thông tin sách lên giao diện.
     * Bao gồm: tiêu đề, tác giả, mô tả, hình ảnh bìa, mã QR, đánh giá trung bình và các bình luận.
     * Nếu là admin và chưa có QR code, sẽ tự động tạo mã mới.
     *
     * @param book đối tượng Document đại diện cho sách đang được xem
     */
    public void setBookData(Document book) {
        this.currentBook = book;
        if (book == null) {
            System.err.println("Book data is null in BookDetailController.");
            Platform.runLater(() -> bookTitleText.setText("Không có thông tin sách"));
            return;
        }

        User currentUser = SessionManager.getCurrentUser();
        boolean isAdmin = currentUser != null && "admin".equalsIgnoreCase(currentUser.getRole());

        Task<Void> loadBookDetailsTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // Generate QR code if needed (only for admin and if it doesn't exist)
                if ((book.getQrCodePath() == null || book.getQrCodePath().isEmpty()) && isAdmin) {
                    try {
                        String newQrCodePath = documentService.regenerateQRCode(book, currentUser);
                        book.setQrCodePath(newQrCodePath);
                        System.out.println("Generated new QR code for ISBN: " + book.getIsbn() + " at: " + newQrCodePath);
                    } catch (Exception e) {
                        System.err.println("Error generating QR code for ISBN: " + book.getIsbn() + ": " + e.getMessage());
                    }
                }

                Image qrImage = loadQRCodeImageInBackground(book.getQrCodePath());
                double avgRating = ReviewDAO.calculateAverageRating(book.getIsbn());
                List<Review> reviews = ReviewDAO.getAllReviewsFromMemory();
                URL placeholderUrl = getClass().getResource("/image/img.png");
                String placeholderPath = (placeholderUrl != null) ? placeholderUrl.toExternalForm() : null;

                Platform.runLater(() -> {
                    bookTitleText.setText(book.getTitle() != null ? book.getTitle() : "N/A");
                    bookAuthorsText.setText(book.getAuthors() != null && book.getAuthors().length > 0 ? "bởi " + String.join(", ", book.getAuthors()) : "bởi Tác giả không xác định");
                    publishDateText.setText(book.getPublishedDate() != null ? book.getPublishedDate() : "N/A");
                    publisherText.setText(book.getPublisher() != null ? book.getPublisher() : "N/A");
                    isbnText.setText(book.getIsbn() != null ? book.getIsbn() : "N/A");
                    descriptionTextArea.setText(book.getDescription() != null && !book.getDescription().isEmpty() ? book.getDescription() : "Không có mô tả.");
                    languageText.setText("Tiếng Anh");

                    // Load book cover image using the updated BookImageLoader
                    BookImageLoader.loadImage(book.getThumbnailUrl(), bookCoverImageView);
                    if (book.getThumbnailUrl() == null || book.getThumbnailUrl().isEmpty() && placeholderPath != null) {
                        bookCoverImageView.setImage(new Image(placeholderPath));
                    }

                    if (qrImage != null) {
                        qrCodeImageView.setImage(qrImage);
                    }
                    updateAvgRatingText(avgRating);
                    loadReviewsOnUI(reviews);
                    System.out.println("Book Title: " + book.getTitle() + ", Quantity: " + book.getQuantity());
                    borrowButton.setDisable(book.getQuantity() == 0);
                });
                return null;
            }

            @Override
            protected void failed() {
                getException().printStackTrace();
                Platform.runLater(() -> AlertUtils.showAlert("Lỗi", "Không thể tải thông tin sách.", Alert.AlertType.ERROR));
            }
        };
        new Thread(loadBookDetailsTask).start();
    }

    /**
     * Cập nhật điểm đánh giá trung bình của sách hiện tại trên giao diện.
     * Được gọi sau khi có đánh giá mới.
     * Thực hiện trong một task nền để lấy dữ liệu từ ReviewDAO.
     */
    private void updateAvgRatingOnUI() {
        if (currentBook == null) return;
        Task<Double> calculateRatingTask = new Task<>() {
            @Override
            protected Double call() throws Exception {
                return ReviewDAO.calculateAverageRating(currentBook.getIsbn());
            }

            @Override
            protected void succeeded() {
                updateAvgRatingText(getValue());
            }

            @Override
            protected void failed() {
                getException().printStackTrace();
                Platform.runLater(() -> avgRatingText.setText("Lỗi tải"));
            }
        };
        new Thread(calculateRatingTask).start();
    }

    /**
     * Cập nhật văn bản hiển thị điểm đánh giá trung bình của sách.
     * Nếu điểm đánh giá lớn hơn 0, hiển thị với định dạng một chữ số thập phân.
     * Ngược lại, hiển thị "Chưa có đánh giá".
     *
     * @param avgRating Điểm đánh giá trung bình.
     */
    private void updateAvgRatingText(double avgRating) {
        if (avgRating > 0) {
            avgRatingText.setText(String.format("%.1f", avgRating));
        } else {
            avgRatingText.setText("Chưa có đánh giá");
        }
    }

    /**
     * Hiển thị danh sách đánh giá và bình luận lên giao diện người dùng
     * cho tài liệu hiện tại. Chỉ thêm đánh giá hoặc bình luận nếu ISBN khớp.
     *
     * @param reviews Danh sách các đánh giá cần hiển thị.
     */
    private void loadReviewsOnUI(List<Review> reviews) {
        if (currentBook == null) return;
        commentsVBox.getChildren().clear();

        for (Review review : reviews) {
            if (review.getDocumentIsbn().equals(currentBook.getIsbn())) {
                String username = "Người dùng #" + review.getUserId();
                Platform.runLater(() -> {
                    if (review.getRating() > 0) {
                        addRatingToUI(username, review.getRating());
                    }
                    if (review.getComment() != null && !review.getComment().isBlank()) {
                        addCommentToUI(username, review.getComment());
                    }
                });
            }
        }
        Platform.runLater(() -> commentsScrollPane.setVvalue(0));
    }

    /**
     * Đóng cửa sổ hiện tại khi người dùng nhấn nút "Đóng".
     */
    @FXML
    private void handleCloseButtonClick() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Thêm bình luận của người dùng vào giao diện.
     *
     * @param username Tên người dùng (hoặc định danh).
     * @param comment  Nội dung bình luận.
     */
    private void addCommentToUI(String username, String comment) {
        VBox commentBox = new VBox(5);
        commentBox.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 10; -fx-border-radius: 5; -fx-background-radius: 5;");

        Label userLabel = new Label(username);
        userLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");

        Text commentText = new Text(comment);
        commentText.setWrappingWidth(commentsScrollPane.getWidth() - 30);

        commentBox.getChildren().addAll(userLabel, commentText);
        commentsVBox.getChildren().add(0, commentBox);
        Platform.runLater(() -> commentsScrollPane.setVvalue(0));
    }

    /**
     * Thêm đánh giá của người dùng vào giao diện.
     *
     * @param username Tên người dùng (hoặc định danh).
     * @param rating   Số điểm đánh giá.
     */
    private void addRatingToUI(String username, Integer rating) {
        VBox ratingBox = new VBox(5);
        ratingBox.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 10; -fx-border-radius: 5; -fx-background-radius: 5;");

        Label userLabel = new Label(username);
        userLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");

        Text ratingText = new Text("Đánh giá: " + rating);
        ratingText.setWrappingWidth(commentsScrollPane.getWidth() - 30);

        ratingBox.getChildren().addAll(userLabel, ratingText);
        commentsVBox.getChildren().add(0, ratingBox);
    }

    /**
     * Xử lý sự kiện khi người dùng (admin) yêu cầu tạo lại mã QR cho sách.
     * Gọi documentService để tạo lại mã QR, cập nhật hình ảnh mới vào giao diện.
     * Hiển thị thông báo thành công hoặc lỗi tương ứng.
     */
    @FXML
    private void handleRegenerateQRButtonClick() {
        if (currentBook == null || documentService == null) return;
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null || !"admin".equalsIgnoreCase(currentUser.getRole())) return;

        Task<Void> qrTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                try {
                    String newQrCodePath = documentService.regenerateQRCode(currentBook, currentUser);
                    currentBook.setQrCodePath(newQrCodePath);
                    Image qrImage = loadQRCodeImageInBackground(newQrCodePath);
                    Platform.runLater(() -> {
                        if (qrImage != null) {
                            qrCodeImageView.setImage(qrImage);
                        }
                        AlertUtils.showAlert("Thành công", "Đã tạo lại mã QR cho sách: " + currentBook.getTitle(), Alert.AlertType.INFORMATION);
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> AlertUtils.showAlert("Lỗi", "Không thể tạo lại mã QR: " + e.getMessage(), Alert.AlertType.ERROR));
                    throw e;
                }
                return null;
            }

            @Override
            protected void failed() {
                getException().printStackTrace();
            }
        };
        new Thread(qrTask).start();
    }

    /**
     * Tải ảnh mã QR từ đường dẫn đã lưu, nếu tệp tồn tại.
     *
     * @param qrCodePath Đường dẫn đến tệp ảnh mã QR.
     * @return Ảnh mã QR, hoặc {@code null} nếu không tìm thấy.
     */
    private Image loadQRCodeImageInBackground(String qrCodePath) {
        if (qrCodePath != null && !qrCodePath.isEmpty()) {
            File qrFile = new File(qrCodePath);
            System.out.println("Checking QR code file: " + qrFile.getAbsolutePath() + ", exists: " + qrFile.exists());
            if (qrFile.exists()) {
                return new Image(qrFile.toURI().toString());
            } else {
                System.err.println("QR code file not found: " + qrCodePath);
                return null;
            }
        } else {
            System.err.println("QR code path is null or empty for ISBN: " + (currentBook != null ? currentBook.getIsbn() : "null"));
            return null;
        }
    }
}