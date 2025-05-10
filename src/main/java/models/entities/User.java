package models.entities;

public class User {

    /**
     * Khai báo thuộc tính cơ bản của user.
     */
    private int id;
    private String username;
    private String password;
    private String email;
    private String phoneNumber;
    private String role;


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

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return this.password;
    }

    public int getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
