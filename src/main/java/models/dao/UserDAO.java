package models.dao;

import models.entities.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

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

    public boolean isUserExists(Connection conn, String username, String email) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE LOWER(TRIM(username)) = LOWER(TRIM(?)) OR LOWER(TRIM(email)) = LOWER(TRIM(?))";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, email);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public User findUserByUsername(Connection conn, String username) {
        String sql = "SELECT id, username, password, email, phone_number, role FROM users WHERE username = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"), // giữ password để service kiểm tra
                        rs.getString("email"),
                        rs.getString("phone_number"),
                        rs.getString("role")
                );
            }
        } catch (SQLException e) {
            System.err.println("Find user error: " + e.getMessage());
        }
        return null;
    }

}
