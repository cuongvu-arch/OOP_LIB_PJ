package models.DatabaseManagement;

import app.User;
import models.DatabaseConnection;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;

public class UserManagement {
    /**
     * Thuộc tính của userManagement.
     */

    private DatabaseConnection connection;

    /**
     * Constructor cho usermanagement.
     */

    public UserManagement(DatabaseConnection connection) {
        this.connection = connection;
    }

    /**
     * phương thức login.
     */

    public User login(String username, String password) {
        String sql = "SELECT id, username, password, email, phone_number, role FROM users WHERE username = ?";

        try (Connection conn = connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password");
                if (BCrypt.checkpw(password, storedHash)) {
                    return new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            null, // Không trả về password
                            rs.getString("email"),
                            rs.getString("phone_number"),
                            rs.getString("role")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Login error: " + e.getMessage());
        }
        return null;
    }

    /**
     * phương thức signup (sử dụng transaction)
     */

    public boolean signup(String username, String password, String email, String phoneNumber) {
        if (!isValidSignupInput(username, password, email, phoneNumber)) {
            return false;
        }

        Connection conn = null;
        try {
            conn = connection.getConnection();
            conn.setAutoCommit(false);

            if (isUserExists(conn, username, email)) {
                conn.rollback();
                return false;
            }

            if (insertUser(conn, username, password, email, phoneNumber)) {
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

    private boolean insertUser(Connection conn, String username, String password,
                               String email, String phoneNumber) throws SQLException {
        String sql = "INSERT INTO users (username, password, email, phone_number, role) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            String hashedPassword = hashPassword(password);

            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);
            stmt.setString(3, email);
            stmt.setString(4, phoneNumber);
            stmt.setString(5, "USER");

            return stmt.executeUpdate() > 0;
        }
    }

    private boolean isUserExists(Connection conn, String username, String email) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE LOWER(TRIM(username)) = LOWER(TRIM(?)) OR LOWER(TRIM(email)) = LOWER(TRIM(?))";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, email);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean isValidSignupInput(String username, String password,
                                      String email, String phoneNumber) {
        username = username != null ? username.trim() : "";
        email = email != null ? email.trim() : "";

        return username.length() >= 4 &&
                password != null && password.length() >= 6 &&
                email.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$") &&
                phoneNumber != null && phoneNumber.matches("^[0-9]{10,15}$");
    }

    /**
     * Hàm băm password (dùng BCrypt).
     */

    private String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    private void rollbackQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                System.err.println("Rollback error: " + ex.getMessage());
            }
        }
    }

    private void closeQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.setAutoCommit(true); // Reset autocommit before closing
                conn.close();
            } catch (SQLException e) {
                System.err.println("Connection close error: " + e.getMessage());
            }
        }
    }
}
