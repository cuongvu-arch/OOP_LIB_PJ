package Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.collections.FXCollections;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.stage.Stage;
import models.dao.ReviewDAO;
import models.viewmodel.BookRatingView;
import models.entities.Document;
import Controller.BookDetailController;
import utils.BookDetailHelper;
import utils.SceneController;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class TablePartController {
    @FXML
    private TableView<BookRatingView> documentTableView;
    @FXML
    private TableColumn<BookRatingView, BookRatingView> documentInfoColumn;

    public void initialize() {
        setupDocumentTable();
        loadTopRatedBooks();
    }

    private void setupDocumentTable() {
        documentInfoColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue()));

        documentInfoColumn.setCellFactory(column -> new TableCell<>() {
            private final ImageView imageView = new ImageView();
            private final Text text = new Text();
            private final HBox hbox = new HBox(10, imageView, text);

            {
                hbox.setStyle("-fx-padding: 5px; -fx-alignment: center-left;");
                imageView.setFitHeight(60);
                imageView.setFitWidth(40);
                imageView.setPreserveRatio(true);
            }

            @Override
            protected void updateItem(BookRatingView vm, boolean empty) {
                super.updateItem(vm, empty);
                if (empty || vm == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    int index = getIndex() + 1;
                    text.setText(index + ". " + vm.titleProperty().get() + "\nISBN: " + vm.isbnProperty().get());

                    Image image = vm.getThumbnailImage();  // <-- Lấy ảnh từ phương thức mới
                    imageView.setImage(image);

                    setGraphic(hbox);
                }
            }
        });

        documentTableView.setItems(FXCollections.observableArrayList());

        //  BẮT SỰ KIỆN CLICK
        documentTableView.setRowFactory(tv -> {
            TableRow<BookRatingView> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 2) {
                    BookRatingView vm = row.getItem();
                    Document doc = vm.getDocument();
                    BookDetailHelper.openBookDetailWindow(doc);
                }
            });
            return row;
        });
    }


    private void loadTopRatedBooks() {
        ReviewDAO reviewDAO = new ReviewDAO();
        List<Document> topDocuments = reviewDAO.getTopRatedDocuments(10);

        List<BookRatingView> viewModels = topDocuments.stream()
                .map(BookRatingView::new)
                .collect(Collectors.toList());

        documentTableView.setItems(FXCollections.observableArrayList(viewModels));
    }
}
