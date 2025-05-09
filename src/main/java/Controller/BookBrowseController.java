package Controller;

import models.entities.Document;
import models.services.DocumentService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import utils.BookImageLoader;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;

public class BookBrowseController {
    @FXML private TextField titleField;
    @FXML private TextField authorField;
    @FXML private TextField publishDateField;
    @FXML private Button searchButton;
    @FXML private FlowPane booksFlowPane;

    private DocumentService documentService;
    private Document currentDocument;

    @FXML
    private void initialize() {
        documentService = new DocumentService();
        booksFlowPane.setVisible(false);
    }

    @FXML
    private void handleSearchButtonClick() {
        String title = titleField.getText().trim();
        String author = authorField.getText().trim();
        String publishDate = publishDateField.getText().trim();

        if (title.isEmpty() && author.isEmpty() && publishDate.isEmpty()) {
            showAlert(AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập ít nhất một tiêu chí tìm kiếm.");
            return;
        }

        resetUIState(false);
        booksFlowPane.getChildren().clear();
        searchButton.setDisable(true);

        try {
            List<Document> searchResults = documentService.searchBooks(title, author, publishDate);

            if (searchResults == null || searchResults.isEmpty()) {
                showAlert(AlertType.INFORMATION, "Không tìm thấy", "Không tìm thấy sách phù hợp với tiêu chí tìm kiếm.");
                booksFlowPane.setVisible(false);
                return;
            }

            booksFlowPane.setVisible(true);
            for (Document doc : searchResults) {
                ImageView bookCover = createBookCover(doc);
                booksFlowPane.getChildren().add(bookCover);
            }

        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Lỗi cơ sở dữ liệu", "Không thể truy vấn cơ sở dữ liệu: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi hệ thống", "Đã xảy ra lỗi không mong muốn: " + e.getMessage());
            e.printStackTrace();
        } finally {
            searchButton.setDisable(false);
        }
    }

    private ImageView createBookCover(Document doc) {
        ImageView coverView = new ImageView();
        coverView.setFitWidth(150);
        coverView.setFitHeight(200);
        coverView.setPreserveRatio(true);

        if (doc.getThumbnailUrl() != null && !doc.getThumbnailUrl().isEmpty()) {
            BookImageLoader.loadImage(doc.getThumbnailUrl(), coverView);
        } else {
            try {
                String placeholderPath = "/image/img.png";
                URL resourceUrl = getClass().getResource(placeholderPath);
                if (resourceUrl == null) {
                    System.err.println("Lỗi: Không tìm thấy tệp ảnh placeholder tại '" + placeholderPath + "'.");
                } else {
                    coverView.setImage(new Image(resourceUrl.toExternalForm()));
                }
            } catch (Exception e) {
                System.err.println("Lỗi khi tải ảnh placeholder: " + e.getMessage());
                e.printStackTrace();
            }
        }

        coverView.setOnMouseClicked(event -> {
            currentDocument = doc;
            openBookDetailWindow(doc);
        });

        return coverView;
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
            showAlert(AlertType.ERROR, "Lỗi", "Không thể mở trang chi tiết sách: " + e.getMessage());
        }
    }

    private void resetUIState(boolean clearFields) {
        currentDocument = null;
        if (clearFields) {
            titleField.clear();
            authorField.clear();
            publishDateField.clear();
        }
    }

    private void showAlert(AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
