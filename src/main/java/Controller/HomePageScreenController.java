package Controller;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import models.dao.DocumentDAO;
import models.dao.ReviewDAO;
import models.data.DatabaseConnection;
import models.entities.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.stage.Stage;
import utils.BookImageLoader;
import utils.SceneController;
import utils.SessionManager;
import models.viewmodel.BookRatingView;
import models.entities.Document;

import java.io.IOException;

import javafx.stage.Stage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;


/**
    @FXML private ChoiceBox<String> adminFunction;
    @FXML private Label adminFunctionText;
    @FXML private Button searchButton;
 */




public class HomePageScreenController {
    /**
     @FXML private ChoiceBox<String> adminFunction;
     @FXML private Label adminFunctionText;
     @FXML private Button searchButton;
     */
    @FXML private GridPane booksGrid;
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Label pageLabel;

    private int currentPage = 1;
    private static final int BOOKS_PER_PAGE = 6;
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

        try (Connection conn = DatabaseConnection.getConnection()) {
            List<Document> books = documentDAO.getBooksPaginated(conn, page, BOOKS_PER_PAGE);

            int col = 0;
            int row = 0;

            for (Document book : books) {
                VBox bookBox = createBookBox(book);
                booksGrid.add(bookBox, col, row);

                col++;
                if (col >= 3) { // 3 cột mỗi hàng
                    col = 0;
                    row++;
                }
            }

            updatePaginationUI(books.size());

        } catch (SQLException e) {
            e.printStackTrace();
            // Xử lý lỗi
        }
    }

    private VBox createBookBox(Document book) {
        VBox bookBox = new VBox(10);
        bookBox.setStyle("-fx-padding: 10; -fx-background-color: #f5f5f5; -fx-border-radius: 5;");

        // Ảnh bìa sách
        ImageView coverView = new ImageView();
        coverView.setFitWidth(180);
        coverView.setFitHeight(220);
        coverView.setPreserveRatio(true);

        if (book.getThumbnailUrl() != null && !book.getThumbnailUrl().isEmpty()) {
            BookImageLoader.loadImage(book.getThumbnailUrl(), coverView);
        } else {
            // Ảnh placeholder nếu không có URL
            coverView.setImage(new Image("/images/book_placeholder.png"));
        }

        // Tiêu đề sách
        Text titleText = new Text(book.getTitle());
        titleText.setWrappingWidth(180);
        titleText.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

        // Tác giả
        String authors = String.join(", ", book.getAuthors());
        Text authorText = new Text("Tác giả: " + authors);
        authorText.setWrappingWidth(180);
        authorText.setStyle("-fx-font-size: 12;");

        bookBox.getChildren().addAll(coverView, titleText, authorText);
        return bookBox;
    }

    private void updatePaginationUI(int booksLoaded) {
        pageLabel.setText("Trang " + currentPage);
        prevButton.setDisable(currentPage <= 1);
        nextButton.setDisable(booksLoaded < BOOKS_PER_PAGE);
    }
}
