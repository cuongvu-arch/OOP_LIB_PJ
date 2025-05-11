package models.services;

import models.dao.UserDAO;
import models.entities.User;
import models.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    private UserDAO userDAOMock;
    private UserService userService;

    @BeforeEach
    public void setUp() {
        userDAOMock = mock(UserDAO.class);
        userService = new UserService(userDAOMock);
    }

    @Test
    public void testIsValidSignupInput_validInput_returnsTrue() {
        boolean result = userService.isValidSignupInput("user123", "password", "email@example.com", "0123456789");
        assertTrue(result);
    }

    @Test
    public void testIsValidSignupInput_invalidEmail_returnsFalse() {
        boolean result = userService.isValidSignupInput("user123", "password", "invalid-email", "0123456789");
        assertFalse(result);
    }

    @Test
    public void testIsValidSignupInput_invalidPhone_returnsFalse() {
        boolean result = userService.isValidSignupInput("user123", "password", "email@example.com", "abc123");
        assertFalse(result);
    }

    @Test
    public void testLogin_validCredentials_returnsUser() throws SQLException {
        String username = "user";
        String password = "pass";
        User dummyUser = new User();
        dummyUser.setUsername(username);

        when(userDAOMock.getUserByUsernameAndPassword(any(Connection.class), eq(username), eq(password)))
                .thenReturn(dummyUser);

        User result = userService.login(username, password);
        assertNotNull(result);
        assertEquals(username, result.getUsername());
    }

    @Test
    public void testLogin_invalidCredentials_returnsNull() throws SQLException {
        when(userDAOMock.getUserByUsernameAndPassword(any(Connection.class), anyString(), anyString()))
                .thenReturn(null);

        User result = userService.login("wrongUser", "wrongPass");
        assertNull(result);
    }

    @Test
    public void testSignup_userAlreadyExists_returnsFalse() throws SQLException {
        when(userDAOMock.isUserExists(any(Connection.class), eq("user123"), eq("email@example.com")))
                .thenReturn(true);

        boolean result = userService.signup("user123", "password", "email@example.com", "0123456789");
        assertFalse(result);
    }
}

