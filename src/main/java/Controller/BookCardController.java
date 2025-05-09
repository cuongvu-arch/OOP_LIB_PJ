package Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class BookCardController {

    @FXML
    private VBox bookCard;

    @FXML
    private ImageView imageView;

    @FXML
    private Label titleLabel;

    @FXML
    private Label returnLabel;

    private Runnable onReturnCallback;

    // Cập nhật thông tin sách và xử lý sự kiện trả sách
    public void setBookInfo(String title, String imagePath, Runnable onReturnCallback) {
        this.titleLabel.setText(title);
        this.imageView.setImage(new Image(imagePath));
        this.onReturnCallback = onReturnCallback;

        returnLabel.setOnMouseClicked(e -> {
            if (onReturnCallback != null) onReturnCallback.run();
        });
    }

    public VBox getCard() {
        return bookCard;
    }
}
