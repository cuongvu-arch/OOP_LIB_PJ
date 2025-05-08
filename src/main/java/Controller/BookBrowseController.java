package Controller;

import models.entities.Document;
import models.services.DocumentService;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import utils.BookImageLoader;

import java.sql.SQLException;
import java.util.List;
import java.net.URL;

public class BookBrowseController {
    @FXML private TextField titleField;
    @FXML private TextField authorField;
    @FXML private TextField publishDateField;
    @FXML private TextArea resultTextArea;
    @FXML private Button searchButton;
    @FXML private FlowPane booksFlowPane;

    private DocumentService documentService;
    private Document currentDocument;

    @FXML
    private void initialize() {
        documentService = new DocumentService();
        resultTextArea.setVisible(false);
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
                    System.err.println("Lỗi: Không tìm thấy tệp ảnh placeholder tại '" + placeholderPath + "'. " +
                            "Hãy đảm bảo tệp tồn tại trong thư mục 'resources/images'.");
                } else {
                    coverView.setImage(new Image(resourceUrl.toExternalForm()));
                }
            } catch (Exception e) {
                System.err.println("Lỗi khi tải ảnh placeholder '" + "/image/img.png" + "': " + e.getMessage());
                e.printStackTrace();
            }
        }

        coverView.setOnMouseClicked(event -> {
            currentDocument = doc;
            resultTextArea.setText(doc.toString());
            resultTextArea.setVisible(true);
        });

        return coverView;
    }

    private void resetUIState(boolean clearFields) {
        resultTextArea.clear();
        resultTextArea.setVisible(false);
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