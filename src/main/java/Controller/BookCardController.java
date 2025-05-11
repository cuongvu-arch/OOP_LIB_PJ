package Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

/**
 * Controller cho một thẻ sách (book card) trong giao diện.
 * Hiển thị thông tin cơ bản của sách như tiêu đề, ảnh và hỗ trợ sự kiện trả sách.
 */
public class BookCardController {

    @FXML
    private VBox bookCard;

    @FXML
    private ImageView imageView;

    @FXML
    private Label titleLabel;

    @FXML
    private Label returnLabel;

    /**
     * Callback được gọi khi người dùng nhấn vào nút/trường "Trả sách".
     */
    private Runnable onReturnCallback;

    /**
     * Thiết lập thông tin sách hiển thị trên thẻ, bao gồm tiêu đề, ảnh và hàm callback khi trả sách.
     *
     * @param title            Tiêu đề của sách.
     * @param imagePath        Đường dẫn ảnh bìa sách.
     * @param onReturnCallback Hàm được gọi khi người dùng nhấn vào nút "Trả".
     */
    public void setBookInfo(String title, String imagePath, Runnable onReturnCallback) {
        this.titleLabel.setText(title);
        this.imageView.setImage(new Image(imagePath));
        this.onReturnCallback = onReturnCallback;

        returnLabel.setOnMouseClicked(e -> {
            if (onReturnCallback != null) onReturnCallback.run();
        });
    }

    /**
     * Trả về VBox chứa toàn bộ giao diện thẻ sách.
     *
     * @return {@link VBox} đại diện cho thẻ sách.
     */
    public VBox getCard() {
        return bookCard;
    }
}
