package app;

public class User {

    /**
     * Khai báo thuộc tính cơ bản của user.
     */
    private int id;
    private String username;
    private String password;
    private String name;
    private String email;
    private String phone_number;
    private String role;


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

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
      this.email = email;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public void setRole (String role) {
        this.role = role;
    }

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
