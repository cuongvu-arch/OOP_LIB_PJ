package utils;

import javafx.scene.control.Alert;

public class AlertUtils {

    public static void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Bạn có thể thêm các phương thức tiện ích khác liên quan đến Alert tại đây nếu cần
}