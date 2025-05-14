package Controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.Node;
import models.entities.Review;
import models.entities.Document;
import utils.AllCommentsDataHolder;
import utils.CommentUIHelper;

import java.util.List;
import java.util.stream.Collectors;

public class AllCommentsController {

    @FXML
    private VBox allCommentsContainer;

    @FXML
    private ScrollPane allCommentsScrollPane;

    private Document currentBook;

    @FXML
    public void initialize() {
        Document book = AllCommentsDataHolder.getCurrentBook();
        List<Review> reviews = AllCommentsDataHolder.getAllReviews();

        if (book != null && reviews != null) {
            setCurrentBook(book);
            loadAllComments(reviews);
        }
    }


    public void setCurrentBook(Document book) {
        this.currentBook = book;
    }

    public void loadAllComments(List<Review> allReviews) {
        if (currentBook == null) return;

        allCommentsContainer.getChildren().clear();

        // Lọc review khớp ISBN và có comment hợp lệ
        List<Review> matchingReviews = allReviews.stream()
                .filter(r -> r.getDocumentIsbn().equals(currentBook.getIsbn()) &&
                        r.getComment() != null &&
                        !r.getComment().isBlank())
                .collect(Collectors.toList());

        for (Review review : matchingReviews) {
            String username = "Người dùng #" + review.getUserId();
            Node commentNode = CommentUIHelper.createCommentNode(username, review.getComment(), allCommentsScrollPane.getWidth());

            Platform.runLater(() -> allCommentsContainer.getChildren().add(commentNode));
        }

        Platform.runLater(() -> allCommentsScrollPane.setVvalue(0));
    }
}
