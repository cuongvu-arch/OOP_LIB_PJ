package utils;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Objects;

/**
 * Lớp tiện ích hỗ trợ tải ảnh bìa sách từ URL và hiển thị vào {@link ImageView} trong JavaFX.
 * <p>
 * Nếu URL không hợp lệ hoặc bị lỗi, một ảnh mặc định (placeholder) sẽ được sử dụng thay thế.
 */
public class BookImageLoader {

    /**
     * Ảnh mặc định hiển thị khi không thể tải ảnh từ URL.
     */
    private static final Image PLACEHOLDER = new Image(
            Objects.requireNonNull(BookImageLoader.class.getResource("/image/img.png")).toExternalForm()
    );

    /**
     * Tải ảnh từ một URL và gán vào một {@link ImageView}. Nếu URL không hợp lệ hoặc tải lỗi,
     * ảnh mặc định sẽ được hiển thị.
     *
     * @param imageUrl  URL của ảnh cần tải.
     * @param imageView {@link ImageView} nơi ảnh sẽ được hiển thị.
     */
    public static void loadImage(String imageUrl, ImageView imageView) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            imageView.setImage(PLACEHOLDER);
            return;
        }

        // Load ảnh trên luồng nền để tránh chặn UI
        new Thread(() -> {
            try {
                Image image = new Image(imageUrl, false); // false = tải đồng bộ trong luồng này

                // Cập nhật giao diện người dùng trên JavaFX Application Thread
                Platform.runLater(() -> imageView.setImage(image));
            } catch (Exception e) {
                Platform.runLater(() -> imageView.setImage(PLACEHOLDER));
            }
        }).start();
    }
}
