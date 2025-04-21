package models.dao;

import models.entities.User;

import java.sql.SQLException;

public interface IUserDAO {
    User login(String username, String password);

    public boolean insertUser(String username, String password,
                              String email, String phoneNumber) throws SQLException;
}
