package Controller;

import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets; // Đã thêm
import javafx.geometry.Pos;   // Đã thêm
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import models.dao.DocumentDAO;
import models.data.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import utils.BookImageLoader;
import models.entities.Document;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class HomePageScreenController {
    private static final int BOOKS_PER_PAGE = 6;
    @FXML
    private GridPane booksGrid;
    @FXML
    private Button prevButton;
    @FXML
    private Button nextButton;
    @FXML
    private Label pageLabel;
    private int currentPage = 1;
    private DocumentDAO documentDAO = new DocumentDAO();

    @FXML
    public void initialize() {
        loadBooks(currentPage);

        nextButton.setOnAction(e -> {
            currentPage++;
            loadBooks(currentPage);
        });

        prevButton.setOnAction(e -> {
            if (currentPage > 1) {
                currentPage--;
                loadBooks(currentPage);
            }
        });
    }

    private void loadBooks(int page) {
        booksGrid.getChildren().clear();

        Task<List<Document>> task = new Task<>() {
            @Override
            protected List<Document> call() throws Exception {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    return documentDAO.getBooksPaginated(conn, page, BOOKS_PER_PAGE);
                }
            }
        };

        task.setOnSucceeded(e -> {
            List<Document> books = task.getValue();
            if (books == null || books.isEmpty()) {
                updatePaginationUI(0);
                return;
            }
            int col = 0;
            int row = 0;
            int numCols = booksGrid.getColumnConstraints().size() > 0 ? booksGrid.getColumnConstraints().size() : 3;


            for (Document book : books) {
                VBox bookBox = createBookBox(book);
                booksGrid.add(bookBox, col, row);

                col++;
                if (col >= numCols) {
                    col = 0;
                    row++;
                }
            }
            updatePaginationUI(books.size());
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            ex.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Không thể tải danh sách sách: " + ex.getMessage());
            alert.showAndWait();
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }


    private VBox createBookBox(Document book) {
        VBox bookBox = new VBox(10);
        bookBox.getStyleClass().add("book-box");
        bookBox.setPadding(new Insets(10)); // Dòng 110
        bookBox.setAlignment(Pos.CENTER);   // Dòng 111
        bookBox.setPrefWidth(200);

        ImageView coverView = new ImageView();
        coverView.setFitWidth(150);
        coverView.setFitHeight(200);
        coverView.setPreserveRatio(true);

        if (book.getThumbnailUrl() != null && !book.getThumbnailUrl().isEmpty()) {
            BookImageLoader.loadImage(book.getThumbnailUrl(), coverView);
        } else {
            try {
                String placeholderPath = "/image/img.png";
                URL resourceUrl = getClass().getResource(placeholderPath);
                if (resourceUrl != null) {
                    coverView.setImage(new Image(resourceUrl.toExternalForm()));
                } else {
                    System.err.println("Không tìm thấy ảnh placeholder cho trang chủ: " + placeholderPath);
                }
            } catch (Exception e) {
                System.err.println("Lỗi tải ảnh placeholder cho trang chủ: " + e.getMessage());
            }
        }

        Text titleText = new Text(book.getTitle() != null ? book.getTitle() : "Không có tiêu đề");
        titleText.getStyleClass().add("book-title-home");
        titleText.setWrappingWidth(180);
        titleText.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);


        String authorsDisplay = "N/A";
        if (book.getAuthors() != null && book.getAuthors().length > 0) {
            authorsDisplay = String.join(", ", book.getAuthors());
        }
        Text authorText = new Text("Tác giả: " + authorsDisplay);
        authorText.getStyleClass().add("book-author-home");
        authorText.setWrappingWidth(180);
        authorText.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);


        bookBox.getChildren().addAll(coverView, titleText, authorText);

        bookBox.setOnMouseClicked(event -> {
            openBookDetailWindow(book);
        });
        bookBox.setOnMouseEntered(e -> bookBox.setStyle("-fx-background-color: #e9e9e9; -fx-border-color: #cccccc; -fx-border-width: 1px; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 2);"));
        bookBox.setOnMouseExited(e -> bookBox.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;"));


        return bookBox;
    }

    private void openBookDetailWindow(Document book) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/BookDetailScreen.fxml"));
            Parent root = loader.load();

            BookDetailController controller = loader.getController();
            controller.setBookData(book);

            Stage detailStage = new Stage();
            detailStage.setTitle("Chi tiết sách: " + (book.getTitle() != null ? book.getTitle() : "Không có tiêu đề"));
            detailStage.setScene(new Scene(root));
            detailStage.initModality(Modality.APPLICATION_MODAL);
            detailStage.setResizable(false);
            detailStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Không thể mở trang chi tiết sách: " + e.getMessage());
            alert.showAndWait();
        }
    }


    private void updatePaginationUI(int booksLoaded) {
        pageLabel.setText("Trang " + currentPage);
        prevButton.setDisable(currentPage <= 1);
        nextButton.setDisable(booksLoaded < BOOKS_PER_PAGE);
    }
}