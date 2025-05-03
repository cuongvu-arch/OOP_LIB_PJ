package models.dao;

import models.data.DatabaseConnection;
import models.entities.BorrowRecord;
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
            List<BorrowRecord> borrowRecords = BorrowRecordDAO.getAll(conn);

            Library.setUserList(users);
            Library.setDocumentList(documents);
            borrowRecords.forEach(Library::addBorrowRecord);

            System.out.println("Dữ liệu thư viện đã được tải vào bộ nhớ.");
        } catch (Exception e) {
            System.err.println("Lỗi khi tải dữ liệu thư viện: " + e.getMessage());
        }
    }

    public void displayAllUsers() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            List<User> users = UserDAO.getAllUser(conn);
            if (users != null && !users.isEmpty()) {
                for (User user : users) {
                    System.out.println("Username: " + user.getUsername() + ", Email: " + user.getEmail());
                }
            } else {
                System.out.println("No users found.");
            }
        } catch (Exception e) {
            System.err.println("Error displaying users: " + e.getMessage());
        }
    }


    public void displayAllDocs() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            List<Document> docs = DocumentDAO.getAllDocs(conn);

            if (docs == null || docs.isEmpty()) {
                System.out.println("Không có sách nào trong thư viện.");
                return;
            }

            System.out.println("=== DANH SÁCH TẤT CẢ SÁCH ===");
            for (Document doc : docs) {
                System.out.println("ISBN: " + doc.getIsbn());
                System.out.println("Tiêu đề: " + doc.getTitle());
                System.out.println("Tác giả: ");
                for (String author : doc.getAuthors()) {
                    System.out.println("  - " + author.replaceAll("\"", "").trim());
                }
                System.out.println("Nhà xuất bản: " + doc.getPublisher());
                System.out.println("Ngày xuất bản: " + doc.getPublishedDate());
                System.out.println("Mô tả: " + doc.getDescription());
                System.out.println("Ảnh bìa (URL): " + doc.getThumbnailUrl());
                System.out.println("-----------------------------------------");
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi hiển thị danh sách sách: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
