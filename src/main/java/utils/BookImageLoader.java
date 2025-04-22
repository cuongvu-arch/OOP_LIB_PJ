package utils;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class BookImageLoader {
    public static void loadImage(String imageUrl, ImageView imageView) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            imageView.setImage(null);
            return;
        }

        try {
            Image image = new Image(imageUrl, true); // true = tải nền
            imageView.setImage(image);
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(200);
            imageView.setFitHeight(300);
        } catch (Exception e) {
            imageView.setImage(null);
        }
    }
}