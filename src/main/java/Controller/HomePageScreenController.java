package Controller;


import models.dao.ReviewDAO;
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
import utils.SceneController;
import utils.SessionManager;
import models.viewmodel.BookRatingView;
import models.entities.Document;

import java.io.IOException;

import javafx.stage.Stage;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class HomePageScreenController {
/**
    @FXML private ChoiceBox<String> adminFunction;
    @FXML private Label adminFunctionText;
    @FXML private Button searchButton;
    @FXML private TableView<BookRatingView> documentTableView; // TableView để hiển thị danh sách tài liệu
    @FXML private TableColumn<BookRatingView, BookRatingView> documentInfoColumn;


    public void initialize() {
        setupDocumentTable();
        loadTopRatedBooks();
    }

    private void setupDocumentTable() {
        documentInfoColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue()));

        documentInfoColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(BookRatingView vm, boolean empty) {
                super.updateItem(vm, empty);
                if (empty || vm == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    int index = getIndex() + 1;
                    String display = index + ". " + vm.titleProperty() + "\nISBN: " + vm.isbnProperty();
                    setText(display);
                    setGraphic(null);
                }
            }
        });

        documentTableView.setItems(FXCollections.observableArrayList());
    }

    private void loadTopRatedBooks() {
        ReviewDAO reviewDAO = new ReviewDAO();
        List<Document> topDocuments = reviewDAO.getTopRatedDocuments(10);

        List<BookRatingView> viewModels = topDocuments.stream()
                .map(BookRatingView::new)
                .collect(Collectors.toList());

        documentTableView.setItems(FXCollections.observableArrayList(viewModels));
    }
 */
}

