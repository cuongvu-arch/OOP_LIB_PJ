package utils;

import models.entities.User;

/**
 * Lớp tiện ích dùng để quản lý phiên làm việc của người dùng trong ứng dụng.
 * Hỗ trợ lưu, truy xuất, và kiểm tra quyền của người dùng hiện tại.
 */
public class SessionManager {
    private static User currentUser;

    // Private constructor để ngăn việc khởi tạo instance từ bên ngoài
    private SessionManager() {
    }

    /**
     * Lấy thông tin người dùng hiện tại trong phiên làm việc.
     *
     * @return Đối tượng {@link User} hiện tại hoặc {@code null} nếu chưa đăng nhập
     */
    public static User getCurrentUser() {
        return currentUser;
    }

    /**
     * Thiết lập người dùng hiện tại cho phiên làm việc.
     *
     * @param user Người dùng đăng nhập
     */
    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    /**
     * Xoá phiên làm việc hiện tại, thường được gọi khi đăng xuất.
     */
    public static void clearSession() {
        currentUser = null;
    }

    /**
     * Kiểm tra xem người dùng hiện tại có phải là admin hay không.
     *
     * @return {@code true} nếu người dùng tồn tại và có vai trò là "admin", ngược lại {@code false}
     */
    public static boolean isAdmin() {
        return currentUser != null && "admin".equalsIgnoreCase(currentUser.getRole());
    }
}
