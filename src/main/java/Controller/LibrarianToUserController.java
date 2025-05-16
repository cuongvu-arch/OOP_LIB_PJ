package Controller;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import models.entities.*;
import utils.SceneController;

import java.util.*;

public class LibrarianToUserController {



    @FXML
    private Button addUserButton;



    @FXML
    private TableView<User> tableView;

    @FXML
    private TableColumn<User, String> usernameColumn;

    @FXML
    private TableColumn<User, String> emailColumn;

    @FXML
    private TableColumn<User, String> phoneNumberColumn;

    @FXML
    private TableColumn<User, String> roleColumn;


    /**
     * Phương thức khởi tạo controller khi giao diện được tải.
     * Thiết lập liên kết cột và khởi động tải dữ liệu người dùng cùng lịch sử mượn sách.
     */
    @FXML
    public void initialize() {
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("Email"));
        phoneNumberColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        loadUser();
    }

    private void  loadUser() {
        Task<ObservableList<User>> task = new Task<>() {
            @Override
            protected ObservableList<User> call() throws Exception {
                ObservableList<User> viewList = FXCollections.observableArrayList();
                    List<User> userList = Library.getUserList();
                    for (User user : userList) {

                        viewList.add(new User(
                                user.getUsername(),
                                user.getEmail(),
                                user.getPhoneNumber(),
                                user.getRole()
                        ));
                    }
                return viewList;
            }
        };

        task.setOnSucceeded(event -> tableView.setItems(task.getValue()));

        task.setOnFailed(event -> {
            System.err.println("Lỗi khi tải dữ liệu người dùng và mượn sách:");
            task.getException().printStackTrace();
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    public void addUser() {
        SceneController.getInstance().switchCenterContent("/fxml/UserManager.fxml");
    }

}
