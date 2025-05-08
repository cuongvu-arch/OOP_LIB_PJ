package utils;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Objects;

public class BookImageLoader {
    private static final Image PLACEHOLDER = new Image(Objects.requireNonNull(BookImageLoader.class.getResource("/image/img.png")).toExternalForm());

    public static void loadImage(String imageUrl, ImageView imageView) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            imageView.setImage(PLACEHOLDER);
            return;
        }

        // Load ảnh trên luồng nền
        new Thread(() -> {
            try {
                Image image = new Image(imageUrl, false); // false = tải đồng bộ trong thread này

                // Sau khi tải, cập nhật UI trên JavaFX thread
                Platform.runLater(() -> imageView.setImage(image));
            } catch (Exception e) {
                Platform.runLater(() -> imageView.setImage(PLACEHOLDER));
            }
        }).start();
    }
}
