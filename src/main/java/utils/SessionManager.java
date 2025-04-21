package utils;

import models.entities.User;

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

    public static void clearSession() {
        currentUser = null;
    }
}