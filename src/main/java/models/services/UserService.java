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

public class UserService {

    private final UserDAO userDAO;

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

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

    public boolean isValidSignupInput(String username, String password, String email, String phoneNumber) {
        return username != null && username.trim().length() >= 4 &&
                password != null && password.length() >= 6 &&
                email != null && email.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$") &&
                phoneNumber != null && phoneNumber.matches("^[0-9]{10,15}$");
    }

    public boolean isValidSignupInput(String username, String email, String phoneNumber) {
        return username != null && username.trim().length() >= 4 &&
                email != null && email.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$") &&
                phoneNumber != null && phoneNumber.matches("^[0-9]{10,15}$");
    }

    private void rollbackQuietly(Connection conn) {
        try {
            if (conn != null) conn.rollback();
        } catch (SQLException e) {
            System.err.println("Rollback error: " + e.getMessage());
        }
    }

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

    public User login(String username, String password) {
        List<User> userList = Library.getUserList();
        for (User user : userList) {
            if (Objects.equals(user.getUsername(), username) && BCrypt.checkpw(password, user.getPassword())) {
                return user;
            }
        }
        return null;
    }

    public boolean editProfile (int currentUserId, String newUserName, String email, String phoneNumber) {
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
}
