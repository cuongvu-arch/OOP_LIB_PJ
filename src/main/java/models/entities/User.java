package models.entities;

public class User {

    /**
     * Khai báo thuộc tính cơ bản của user.
     */
    private final int id;
    private final String username;
    private final String password;
    private final String email;
    private final String phone_number;
    private final String role;


    /**
     * Tạo constructor.
     */

    public User(int id, String username, String password, String email, String phone_number, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.phone_number = phone_number;
        this.role = role;

    }

    /**
     * tạo các phương thức get/set.
     */

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }


    public String getEmail() {
        return email;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public String getRole() {
        return role;
    }
}
