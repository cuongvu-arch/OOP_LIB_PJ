package Controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
import utils.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller cho giao diện chi tiết sách trong ứng dụng JavaFX.
 * Cho phép người dùng xem thông tin chi tiết của một cuốn sách, đánh giá, bình luận và thực hiện hành động mượn sách.
 * Nếu là admin, người dùng còn có thể tái tạo mã QR cho sách.
 */
public class BookDetailController {
    @FXML
    private Button regenerateQRButton;
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
    private VBox commentsVBox;
    @FXML
    private TextArea newCommentTextArea;
    @FXML
    private ScrollPane commentsScrollPane;
    @FXML
    private Button borrowButton;
    @FXML
    private Label star1, star2, star3, star4, star5;

    private Document currentBook;
    private DocumentService documentService;
    private ReviewDAO reviewDAO = new ReviewDAO(); // Khởi tạo DAO
    private Connection conn = DatabaseConnection.getConnection();
    private List<Review> allReviews = reviewDAO.getAllReviews(conn);
    private List<Label> stars;
    private int currentRating = 0;


    /**
     * Khởi tạo controller, tải dữ liệu cần thiết như danh sách đánh giá và cấu hình quyền admin.
     * Thực hiện trên thread nền để tránh chặn giao diện.
     */
    public void initialize() {
        stars = Arrays.asList(star1, star2, star3, star4, star5);

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


    private void updateStarDisplay(int rating) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận đánh giá");
        alert.setHeaderText(null);
        alert.setContentText("Bạn có chắc muốn đánh giá " + rating + " sao không?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            for (int i = 0; i < stars.size(); i++) {
                stars.get(i).setText(i < rating ? "★" : "☆");
            }
            currentRating = rating;
            saveRatingToDatabase(rating);
        }
    }

    private void saveRatingToDatabase(int rating) {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null || currentBook == null) return;

        Review newReview = new Review(
                currentUser.getId(),
                currentBook.getIsbn(),
                rating,
                "", // comment để trống, chỉ update rating
                LocalDateTime.now()
        );

        Task<Void> saveTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                ReviewDAO.addOrUpdateReview(newReview);
                return null;
            }

            @Override
            protected void succeeded() {
                updateAvgRatingOnUI();
            }

            @Override
            protected void failed() {
                getException().printStackTrace();
                Platform.runLater(() -> AlertUtils.showAlert("Lỗi", "Không thể lưu đánh giá.", Alert.AlertType.ERROR));
            }
        };

        new Thread(saveTask).start();
    }

    @FXML private void handleStar1Click() { updateStarDisplay(1); }
    @FXML private void handleStar2Click() { updateStarDisplay(2); }
    @FXML private void handleStar3Click() { updateStarDisplay(3); }
    @FXML private void handleStar4Click() { updateStarDisplay(4); }
    @FXML private void handleStar5Click() { updateStarDisplay(5); }

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
            avgRatingText.setText(String.format("%.1f ★", avgRating));
        } else {
            avgRatingText.setText("Chưa có đánh giá");
        }
    }

    private void loadReviewsOnUI(List<Review> reviews) {
        if (currentBook == null) return;
        commentsVBox.getChildren().clear();

        List<Review> matchingComments = new ArrayList<>();

        // Lọc các review có comment hợp lệ và ISBN khớp
        for (Review review : reviews) {
            if (review.getDocumentIsbn().equals(currentBook.getIsbn()) &&
                    review.getComment() != null &&
                    !review.getComment().isBlank()) {
                matchingComments.add(review);
            }
        }

        // Chỉ lấy tối đa 3 comment
        List<Review> top3Comments = matchingComments.stream()
                .limit(3)
                .collect(Collectors.toList());

        for (Review review : top3Comments) {
            String username = "Người dùng #" + review.getUserId();
            Platform.runLater(() -> addCommentToUI(username, review.getComment()));
        }

        Platform.runLater(() -> commentsScrollPane.setVvalue(0));
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

                    boolean isCurrentlyBorrowing = borrowRecordDAO.isCurrentlyBorrowing(
                            conn, currentUser.getId(), selectedDoc.getIsbn()
                    );

                    if (isCurrentlyBorrowing) {
                        Platform.runLater(() -> AlertUtils.showAlert(
                                "Thông báo", "Bạn đang mượn sách này và chưa trả.", Alert.AlertType.INFORMATION
                        ));
                    } else {
                        BorrowRecord borrowRecord = new BorrowRecord(
                                currentUser.getId(),
                                selectedDoc.getIsbn(),
                                new java.sql.Date(new Date().getTime()),
                                null,
                                "Đang mượn"
                        );
                        borrowRecordDAO.add(conn, borrowRecord);
                        Platform.runLater(() -> AlertUtils.showAlert(
                                "Thông báo", "Đã mượn sách: " + selectedDoc.getTitle(), Alert.AlertType.INFORMATION
                        ));
                    }
                }
                return null;
            }

            @Override
            protected void failed() {
                getException().printStackTrace();
                Platform.runLater(() -> AlertUtils.showAlert(
                        "Lỗi", "Không thể thực hiện mượn sách.", Alert.AlertType.ERROR
                ));
            }
        };
        new Thread(borrowTask).start();
    }

    @FXML
    private void handleViewAllComments() {
        if (currentBook == null) return;

        // Truyền dữ liệu qua holder
        AllCommentsDataHolder.setCurrentBook(currentBook);
        AllCommentsDataHolder.setAllReviews(allReviews);

        // Mở giao diện mới bằng Stage
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AllCommentsView.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Tất cả bình luận");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                    ReviewDAO.addReview(review); // Lưu vào DB
                    Platform.runLater(() -> {
                        allReviews.add(review); // Cập nhật vào danh sách tất cả comment

                        // Nếu số comment hiển thị trong VBox (bên trong ScrollPane) < 3 thì thêm vào UI
                        if (commentsVBox.getChildren().size() < 3) {
                            addCommentToUI(currentUser.getUsername(), commentText);
                        }

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