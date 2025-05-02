package models.dao;

import models.data.DatabaseConnection;
import models.entities.Document;
import models.entities.Library;
import models.entities.User;

import java.sql.Connection;
import java.util.List;

public class LibraryDAO {
    public void loadLibraryData() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            List<User> users = UserDAO.getAllUser(conn);
            List<Document> documents = DocumentDAO.getAllDocs(conn);

            Library.setUserList(users);
            Library.setDocumentList(documents);

            System.out.println("Dữ liệu thư viện đã được tải vào bộ nhớ.");
        } catch (Exception e) {
            System.err.println("Lỗi khi tải dữ liệu thư viện: " + e.getMessage());
        }
    }

}
