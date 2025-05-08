package Controller;

import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import models.dao.DocumentDAO;
import models.data.DatabaseConnection;
import javafx.fxml.FXML;

import javafx.scene.control.*;
import utils.BookImageLoader;
import models.entities.Document;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;




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
            int col = 0;
            int row = 0;

            for (Document book : books) {
                VBox bookBox = createBookBox(book);
                booksGrid.add(bookBox, col, row);

                col++;
                if (col >= 3) {
                    col = 0;
                    row++;
                }
            }

            updatePaginationUI(books.size());
        });

        task.setOnFailed(e -> {
            task.getException().printStackTrace();
        });

        // Chạy task trong background thread
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }


    private VBox createBookBox(Document book) {
        VBox bookBox = new VBox(10);
        bookBox.setStyle("-fx-padding: 10; -fx-background-color: #f5f5f5; -fx-border-radius: 5;");

        ImageView coverView = new ImageView();
        coverView.setFitWidth(150);
        coverView.setFitHeight(200);
        coverView.setPreserveRatio(true);

        if (book.getThumbnailUrl() != null && !book.getThumbnailUrl().isEmpty()) {
            BookImageLoader.loadImage(book.getThumbnailUrl(), coverView);
        } else {

            coverView.setImage(new Image("/images/book_placeholder.png"));
        }


        Text titleText = new Text(book.getTitle());
        titleText.setWrappingWidth(180);
        titleText.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

        String authors = String.join(", ", book.getAuthors());
        Text authorText = new Text("Tác giả: " + authors);
        authorText.setWrappingWidth(150);
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
