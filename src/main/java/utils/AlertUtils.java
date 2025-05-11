package utils;

import javafx.scene.control.Alert;

/**
 * Lớp tiện ích hỗ trợ hiển thị các hộp thoại thông báo (Alert) trong JavaFX.
 * <p>
 * Cung cấp phương thức dùng chung để hiển thị các thông báo lỗi, thông tin, cảnh báo, xác nhận,...
 */
public class AlertUtils {

    /**
     * Hiển thị một hộp thoại thông báo với tiêu đề, nội dung và loại cảnh báo được chỉ định.
     *
     * @param title     Tiêu đề của hộp thoại.
     * @param message   Nội dung thông báo.
     * @param alertType Loại hộp thoại (INFORMATION, WARNING, ERROR, CONFIRMATION, ...).
     */
    public static void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null); // Không hiển thị tiêu đề phụ
        alert.setContentText(message);
        alert.showAndWait(); // Hiển thị và chờ người dùng đóng hộp thoại
    }

    // Bạn có thể thêm các phương thức tiện ích khác liên quan đến Alert tại đây nếu cần
}
