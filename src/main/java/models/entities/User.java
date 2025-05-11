package models.entities;

/**
 * Represents a user of the library system.
 * This class stores user information including ID, username, password, contact info, and role.
 */
public class User {

    /**
     * The unique ID of the user.
     */
    private int id;

    /**
     * The username used to log in.
     */
    private String username;

    /**
     * The password for the user's account.
     */
    private String password;

    /**
     * The user's email address.
     */
    private String email;

    /**
     * The user's phone number.
     */
    private String phoneNumber;

    /**
     * The role of the user (e.g., "admin", "user").
     */
    private String role;

    /**
     * Constructs a {@code User} object with all attributes specified.
     *
     * @param id          the user ID
     * @param username    the username
     * @param password    the password
     * @param email       the email address
     * @param phoneNumber the phone number
     * @param role        the role of the user (e.g., admin, user)
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
     * Default constructor.
     */
    public User() {
    }

    public User(int i, String oldUser, String mail, String number, String user) {
    }

    /**
     * Returns the user ID.
     *
     * @return the ID
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the user ID.
     *
     * @param i the new ID
     */
    public void setId(int i) {
        this.id = i;
    }

    /**
     * Returns the username.
     *
     * @return the username
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Sets the username.
     *
     * @param username the new username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns the password.
     *
     * @return the password
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Returns the user's email.
     *
     * @return the email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the user's email.
     *
     * @param email the new email address
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns the user's role.
     *
     * @return the role
     */
    public String getRole() {
        return role;
    }

    /**
     * Sets the user's role.
     *
     * @param role the new role (e.g., "admin", "user")
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Returns the user's phone number.
     *
     * @return the phone number
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Sets the user's phone number.
     *
     * @param phoneNumber the new phone number
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
