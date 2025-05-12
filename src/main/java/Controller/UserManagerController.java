package Controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import models.dao.UserDAO;
import models.data.DatabaseConnection;
import models.entities.User;
import models.services.UserService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class UserManagerController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private PasswordField passwordField;

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, String> nameColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> phoneColumn;

    @FXML private Button searchButton;
    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;

    @FXML private Label statusLabel;

    private ObservableList<User> userList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getUsername()));
        emailColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getEmail()));
        phoneColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getPhoneNumber()));

        userTable.setItems(userList);

        // Ban đầu vô hiệu hóa nút sửa và xóa
        editButton.setDisable(true);
        deleteButton.setDisable(true);

        userTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                // Kích hoạt nút sửa và xóa khi có người dùng được chọn
                editButton.setDisable(false);
                deleteButton.setDisable(false);
                nameField.setText(newSelection.getUsername());
                emailField.setText(newSelection.getEmail());
                phoneField.setText(newSelection.getPhoneNumber());
            } else {
                // Vô hiệu hóa nút sửa và xóa khi không có người dùng nào được chọn
                editButton.setDisable(true);
                deleteButton.setDisable(true);
                clearForm(); // Có thể clear form khi không có selection
            }
        });
    }

    @FXML
    public void handleSearchUser() {
        String keyword = nameField.getText().trim().toLowerCase();

        if (keyword.isEmpty()) {
            userList.clear();
            statusLabel.setText("Vui lòng nhập tên để tìm.");
            // Vô hiệu hóa nút sửa và xóa khi không có kết quả tìm kiếm
            editButton.setDisable(true);
            deleteButton.setDisable(true);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            List<User> users = UserDAO.getAllUser(conn);
            assert users != null;
            List<User> filtered = users.stream()
                    .filter(user -> user.getUsername().toLowerCase().contains(keyword))
                    .toList();

            userList.setAll(filtered);

            if (filtered.isEmpty()) {
                statusLabel.setText("Không tìm thấy người dùng.");
                // Vô hiệu hóa nút sửa và xóa khi không có kết quả tìm kiếm
                editButton.setDisable(true);
                deleteButton.setDisable(true);
            } else {
                statusLabel.setText("Đã tìm thấy " + filtered.size() + " người dùng.");
                // Kích hoạt nút sửa và xóa khi có kết quả tìm kiếm
                if (!userTable.getSelectionModel().isEmpty()) {
                    editButton.setDisable(false);
                    deleteButton.setDisable(false);
                } else {
                    editButton.setDisable(true);
                    deleteButton.setDisable(true);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Lỗi khi tìm kiếm.");
            // Vô hiệu hóa nút sửa và xóa khi có lỗi
            editButton.setDisable(true);
            deleteButton.setDisable(true);
        }
    }

    @FXML
    public void handleAddUser() {
        // Logic thêm người dùng vẫn giữ nguyên
        String username = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            UserService userService = new UserService(new UserDAO());
            boolean success = userService.signup(username, password, email, phone);
            if (success) {
                statusLabel.setText("Đã thêm người dùng.");
                handleSearchUser(); // Tự động tìm kiếm lại để cập nhật bảng
                clearForm();
            } else {
                statusLabel.setText("Thêm người dùng thất bại.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Lỗi cơ sở dữ liệu.");
        }
    }

    @FXML
    public void handleEditUser() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Vui lòng chọn người dùng để sửa.");
            return;
        }

        String newName = nameField.getText().trim();
        String newEmail = emailField.getText().trim();
        String newPhone = phoneField.getText().trim();

        if (newName.isEmpty() || newEmail.isEmpty() || newPhone.isEmpty()) {
            statusLabel.setText("Thông tin không được để trống.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            UserService userService = new UserService(new UserDAO());
            boolean updated = userService.editProfile(selected.getId(), newName, newEmail, newPhone);
            if (updated) {
                statusLabel.setText("Cập nhật thành công.");
                handleSearchUser();
                clearForm();
            } else {
                statusLabel.setText("Cập nhật thất bại.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Lỗi khi cập nhật.");
        }
    }

    @FXML
    public void handleDeleteUser() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Vui lòng chọn người dùng để xóa.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText("Bạn có chắc muốn xóa người dùng: " + selected.getUsername() + "?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                PreparedStatement stmt = conn.prepareStatement("DELETE FROM users WHERE id = ?");
                stmt.setInt(1, selected.getId());
                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    statusLabel.setText("Đã xóa người dùng.");
                    userList.remove(selected);
                    clearForm();
                    // Sau khi xóa, có thể vô hiệu hóa lại nút sửa và xóa nếu không còn user nào được chọn
                    if (userTable.getItems().isEmpty()) {
                        editButton.setDisable(true);
                        deleteButton.setDisable(true);
                    }
                } else {
                    statusLabel.setText("Không thể xóa.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                statusLabel.setText("Lỗi khi xóa.");
            }
        }
    }

    private void clearForm() {
        nameField.clear();
        emailField.clear();
        phoneField.clear();
        passwordField.clear();
        userTable.getSelectionModel().clearSelection();
    }
}