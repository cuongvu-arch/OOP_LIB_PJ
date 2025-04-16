package utils;

import app.User;

public class SessionManager {
    private static User currentUser;

    private SessionManager() {
    }


    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public void clearSession() {
        currentUser = null;
    }
}