package models.entities;

public class User {

    /**
     * Khai báo thuộc tính cơ bản của user.
     */
    private final int id;
    private final String username;
    private final String password;
    private final String email;
    private final String phoneNumber;
    private final String role;


    /**
     * Tạo constructor.
     */

    public User(int id, String username, String password, String email, String phoneNumber, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.phoneNumber = phoneNumber;
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

    public int getId() { return id; }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public String getPhoneNumber() { return phoneNumber;}
}
