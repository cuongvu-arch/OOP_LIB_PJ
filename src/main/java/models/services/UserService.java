package models.services;

import Controller.ProfileScreenController;
import javafx.concurrent.Task;
import models.dao.UserDAO;
import models.data.DatabaseConnection;
import models.entities.Library;
import models.entities.User;
import org.mindrot.jbcrypt.BCrypt;
import utils.SessionManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import static models.data.DatabaseConnection.*;

/**
 * Cung cấp các chức năng nghiệp vụ liên quan đến người dùng,
 * bao gồm đăng ký, đăng nhập và chỉnh sửa thông tin cá nhân.
 */
public class UserService {

    private final UserDAO userDAO;

    /**
     * Khởi tạo UserService với một đối tượng UserDAO.
     *
     * @param userDAO Đối tượng DAO dùng để thao tác với dữ liệu người dùng.
     */
    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Đăng ký tài khoản mới.
     *
     * @param username    Tên đăng nhập.
     * @param password    Mật khẩu.
     * @param email       Email của người dùng.
     * @param phoneNumber Số điện thoại.
     * @return true nếu đăng ký thành công, ngược lại false.
     */
    public boolean signup(String username, String password, String email, String phoneNumber) {
        if (!isValidSignupInput(username, password, email, phoneNumber)) {
            return false;
        }

        Connection conn = null;
        try {
            conn = getConnection();
            if (conn == null) return false;

            conn.setAutoCommit(false);

            if (userDAO.isUserExists(conn, username, email)) {
                conn.rollback();
                return false;
            }

            if (userDAO.insertUser(conn, username, password, email, phoneNumber)) {
                conn.commit();
                return true;
            } else {
                conn.rollback();
                return false;
            }
        } catch (SQLException e) {
            rollbackQuietly(conn);
            System.err.println("Signup error: " + e.getMessage());
            return false;
        } finally {
            closeQuietly(conn);
        }
    }


    /**
     * Kiểm tra tính hợp lệ của thông tin đăng ký.
     *
     * @param username    Tên người dùng.
     * @param password    Mật khẩu.
     * @param email       Email.
     * @param phoneNumber Số điện thoại.
     * @return true nếu hợp lệ, ngược lại false.
     */
    public boolean isValidSignupInput(String username, String password, String email, String phoneNumber) {
        return username != null && username.trim().length() >= 4 &&
                password != null && password.length() >= 6 &&
                email != null && email.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$") &&
                phoneNumber != null && phoneNumber.matches("^[0-9]{10,15}$");
    }

    /**
     * Kiểm tra thông tin đăng ký không bao gồm mật khẩu.
     *
     * @param username    Tên người dùng.
     * @param email       Email.
     * @param phoneNumber Số điện thoại.
     * @return true nếu hợp lệ, ngược lại false.
     */
    public boolean isValidSignupInput(String username, String email, String phoneNumber) {
        return username != null && username.trim().length() >= 4 &&
                email != null && email.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$") &&
                phoneNumber != null && phoneNumber.matches("^[0-9]{10,15}$");
    }

    /**
     * Rollback kết nối một cách an toàn.
     *
     * @param conn Kết nối cơ sở dữ liệu.
     */
    private void rollbackQuietly(Connection conn) {
        try {
            if (conn != null) conn.rollback();
        } catch (SQLException e) {
            System.err.println("Rollback error: " + e.getMessage());
        }
    }

    /**
     * Đóng kết nối một cách an toàn.
     *
     * @param conn Kết nối cơ sở dữ liệu.
     */
    private void closeQuietly(Connection conn) {
        try {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        } catch (SQLException e) {
            System.err.println("Close error: " + e.getMessage());
        }
    }

    /**
     * Đăng nhập người dùng.
     *
     * @param username Tên đăng nhập.
     * @param password Mật khẩu.
     * @return Đối tượng User nếu đăng nhập thành công, ngược lại null.
     */
    public User login(String username, String password) {
        Connection conn = null;
        try {
            conn = getConnection();
            if (conn == null) return null;

            return userDAO.getUserByUsernameAndPassword(conn, username, password);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            closeQuietly(conn);
        }
    }

    /**
     * Cập nhật thông tin hồ sơ người dùng.
     *
     * @param currentUserId ID của người dùng hiện tại.
     * @param newUserName   Tên người dùng mới.
     * @param email         Email mới.
     * @param phoneNumber   Số điện thoại mới.
     * @return true nếu cập nhật thành công, ngược lại false.
     */
    public boolean editProfile(int currentUserId, String newUserName, String email, String phoneNumber) {
        if (!isValidSignupInput(newUserName, email, phoneNumber)) {
            return false;
        }
        Connection conn = null;
        try {
            conn = getConnection();
            if (conn == null) return false;

            conn.setAutoCommit(false);

            if (userDAO.updateUserProfile(conn, currentUserId, newUserName, email, phoneNumber)) {
                conn.commit();

                return true;
            } else {
                conn.rollback();
                return false;
            }
        } catch (SQLException e) {
            rollbackQuietly(conn);
            System.err.println("Edit Error!" + e.getMessage());
            return false;
        } finally {
            closeQuietly(conn);
        }
    }

    /**
     * Phương thức để thêm người dùng trong admin func.
     */

}
