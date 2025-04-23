package models.entities;

import java.util.ArrayList;
import java.util.List;

public class Library {
    private static List<User> userList ;

    public static List<User> getUserList() {
        return userList;
    }

    public static void setUserList(List<User> userList) {
        Library.userList = userList;
    }
}
