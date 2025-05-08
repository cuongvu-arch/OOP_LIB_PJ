package models.dao;

import models.entities.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

    public static List<User> getAllUser(Connection connection) {
        List<User> Result = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while(resultSet.next()) {
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

    public boolean updateUserProfile(Connection connection, String currentUser, String newUserName, String email, String phoneNumber) throws SQLException {
        String sql = "UPDATE users SET username = ?, email = ?, phone_number = ?  WHERE username = ?;";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, newUserName);
            preparedStatement.setString(2,email);
            preparedStatement.setString(3, phoneNumber);
            preparedStatement.setString(4, currentUser);
            return preparedStatement.executeUpdate() > 0;
        }
    }

}
