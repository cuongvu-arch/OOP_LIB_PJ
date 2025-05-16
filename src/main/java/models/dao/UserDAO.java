package models.dao;

import models.data.DatabaseConnection;
import models.entities.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) cho thực thể User.
 * Thực hiện các thao tác liên quan đến cơ sở dữ liệu cho người dùng như:
 * đăng ký, đăng nhập, cập nhật hồ sơ, kiểm tra tồn tại, và lấy danh sách người dùng.
 */
public class UserDAO {

    /**
     * Lấy toàn bộ danh sách người dùng trong hệ thống.
     *
     * @param connection Kết nối đến cơ sở dữ liệu
     * @return Danh sách các đối tượng User, hoặc null nếu có lỗi xảy ra
     */
    public List<User> getAllUser(Connection connection) {
        List<User> Result = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Result.add(new User(
                        resultSet.getInt("id"),
                        resultSet.getString("username"),
                        resultSet.getString("password"),
                        resultSet.getString("email"),
                        resultSet.getString("phone_number"),
                        resultSet.getString("role")));
            }
            return Result;
        } catch (SQLException e) {
            System.err.println("Find user error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Thêm một người dùng mới vào cơ sở dữ liệu với vai trò mặc định là "USER".
     * Mật khẩu được mã hoá bằng BCrypt.
     *
     * @param conn        Kết nối đến cơ sở dữ liệu
     * @param username    Tên người dùng
     * @param password    Mật khẩu (chưa mã hoá)
     * @param email       Email của người dùng
     * @param phoneNumber Số điện thoại
     * @return true nếu thêm thành công, false nếu thất bại
     * @throws SQLException Nếu có lỗi khi thao tác với cơ sở dữ liệu
     */
    public boolean insertUser(Connection conn, String username, String password,
                              String email, String phoneNumber) throws SQLException {
        String sql = "INSERT INTO users (username, password, email, phone_number, role) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);
            stmt.setString(3, email);
            stmt.setString(4, phoneNumber);
            stmt.setString(5, "USER");

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Lấy thông tin người dùng nếu tên đăng nhập và mật khẩu khớp.
     * Mật khẩu đầu vào sẽ được so sánh với bản mã hoá trong DB.
     *
     * @param conn     Kết nối đến cơ sở dữ liệu
     * @param username Tên người dùng
     * @param password Mật khẩu chưa mã hoá
     * @return Đối tượng User nếu đăng nhập thành công, null nếu thất bại
     * @throws SQLException Nếu có lỗi khi thao tác với cơ sở dữ liệu
     */
    public User getUserByUsernameAndPassword(Connection conn, String username, String password) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String hashedPassword = rs.getString("password");
                if (BCrypt.checkpw(password, hashedPassword)) {
                    return new User(
                            rs.getInt("Id"),
                            rs.getString("username"),
                            hashedPassword,
                            rs.getString("email"),
                            rs.getString("phone_number"),
                            rs.getString("role")
                    );
                }
            }
            return null;
        }
    }

    /**
     * Kiểm tra xem người dùng có tồn tại dựa trên tên đăng nhập hoặc email.
     * So sánh không phân biệt chữ hoa - thường và loại bỏ khoảng trắng dư.
     *
     * @param connection Kết nối đến cơ sở dữ liệu
     * @param username   Tên người dùng
     * @param email      Địa chỉ email
     * @return true nếu người dùng tồn tại, false nếu không
     * @throws SQLException Nếu có lỗi khi thao tác với cơ sở dữ liệu
     */
    public boolean isUserExists(Connection connection, String username, String email) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE LOWER(TRIM(username)) = LOWER(TRIM(?)) OR LOWER(TRIM(email)) = LOWER(TRIM(?))";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, email);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Cập nhật hồ sơ người dùng bao gồm tên người dùng, email, và số điện thoại.
     *
     * @param connection    Kết nối đến cơ sở dữ liệu
     * @param currentUserId ID của người dùng hiện tại
     * @param newUserName   Tên người dùng mới
     * @param email         Email mới
     * @param phoneNumber   Số điện thoại mới
     * @return true nếu cập nhật thành công, false nếu thất bại
     * @throws SQLException Nếu có lỗi khi thao tác với cơ sở dữ liệu
     */
    public boolean updateUserProfile(Connection connection, int currentUserId, String newUserName, String email, String phoneNumber) throws SQLException {
        String sql = "UPDATE users SET username = ?, email = ?, phone_number = ?  WHERE Id = ?;";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, newUserName);
            preparedStatement.setString(2, email);
            preparedStatement.setString(3, phoneNumber);
            preparedStatement.setInt(4, currentUserId);
            return preparedStatement.executeUpdate() > 0;
        }
    }

    public boolean deleteUser(int id) {
        if (id == 0) {
            System.err.println("Lỗi khi xóa người dùng: id không hợp lệ");
            return false;
        }
        String deleteBorrowRecords = "DELETE FROM borrow_records WHERE user_id = ?";
        String deleteReviews = "DELETE FROM review WHERE user_id = ?";
        String deleteUser = "DELETE FROM users WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            assert conn != null;
            conn.setAutoCommit(false); // Bắt đầu transaction

            try (PreparedStatement stmt1 = conn.prepareStatement(deleteBorrowRecords);
                 PreparedStatement stmt2 = conn.prepareStatement(deleteReviews);
                 PreparedStatement stmt3 = conn.prepareStatement(deleteUser)) {

                // Xóa borrow_records trước
                stmt1.setInt(1, id);
                stmt1.executeUpdate();

                // Xóa reviews liên quan đến người dùng
                stmt2.setInt(1, id);
                stmt2.executeUpdate();

                // Cuối cùng xóa người dùng
                stmt3.setInt(1, id);
                int rowsAffected = stmt3.executeUpdate();

                conn.commit();
                return rowsAffected > 0;

            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Lỗi khi xóa người dùng (Id: " + id + "): " + e.getMessage());
                e.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            System.err.println("Lỗi kết nối cơ sở dữ liệu khi xóa người dùng.");
            e.printStackTrace();
            return false;
        }
    }

}
