package Controller;

import models.dao.ReviewDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.ReadOnlyObjectWrapper;
import models.viewmodel.BookRatingView;
import models.entities.Document;

import java.util.List;
import java.util.stream.Collectors;

public class TablePartController {
    @FXML
    private TableView<BookRatingView> documentTableView; // TableView để hiển thị danh sách tài liệu
    @FXML
    private TableColumn<BookRatingView, BookRatingView> documentInfoColumn;

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
                    String display = index + ". " + vm.titleProperty().get() + "\nISBN: " + vm.isbnProperty().get();
                    setText(display);
                    setGraphic(null);
                }
            }
        });

        documentTableView.setItems(FXCollections.observableArrayList());
    }

    private void loadTopRatedBooks() {
        // Lấy dữ liệu thực từ cơ sở dữ liệu
        ReviewDAO reviewDAO = new ReviewDAO();
        List<Document> topDocuments = reviewDAO.getTopRatedDocuments(10);

        // Chuyển đổi các Document thành BookRatingView (ViewModel) để hiển thị trong TableView
        List<BookRatingView> viewModels = topDocuments.stream()
                .map(BookRatingView::new)
                .collect(Collectors.toList());

        // Cập nhật dữ liệu vào TableView
        documentTableView.setItems(FXCollections.observableArrayList(viewModels));
    }
}
