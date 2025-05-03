package models.services;

import models.dao.DocumentDAO;
import models.dao.UserDAO;
import models.entities.BorrowRecord;
import models.entities.Document;
import models.entities.Library;
import models.entities.User;

import java.sql.Connection;
import java.util.List;

public class LibraryService {
    public void displayAllUsers() {
        try {
            // Gọi phương thức từ UserDAO để lấy tất cả người dùng
            List<User> users = UserDAO.getAllUser();
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
        try {
            // Gọi phương thức từ DocumentDAO để lấy tất cả tài liệu
            List<Document> docs = DocumentDAO.getAllDocs();
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


    public void displayAllUsersWithBorrowedBooks() {
        for (User user : Library.getUserList()) {
            System.out.println("Username: " + user.getUsername());

            List<String> borrowedBooks = Library.getBorrowRecords().stream()
                    .filter(r -> r.getUserId() == user.getId())
                    .map(BorrowRecord::getIsbn)
                    .toList();

            if (borrowedBooks.isEmpty()) {
                System.out.println("  - Chưa mượn sách nào");
            } else {
                System.out.println("  - Đã mượn các sách với ISBN: " + String.join(", ", borrowedBooks));
            }
        }
    }

    public boolean borrowBook(int userId, String isbn) {
        User user = Library.getUserList().stream()
                .filter(u -> u.getId() == userId)
                .findFirst().orElse(null);

        Document doc = Library.getDocumentList().stream()
                .filter(d -> d.getIsbn().equals(isbn))
                .findFirst().orElse(null);

        if (user == null || doc == null) {
            System.out.println("Người dùng hoặc sách không tồn tại.");
            return false;
        }

        boolean alreadyBorrowed = Library.getBorrowRecords().stream()
                .anyMatch(r -> r.getUserId() == userId && r.getIsbn().equals(isbn));

        if (alreadyBorrowed) {
            System.out.println("Bạn đã mượn sách này rồi.");
            return false;
        }

        Library.addBorrowRecord(new BorrowRecord(userId, isbn));
        System.out.println("Mượn sách thành công.");
        return true;
    }

    public boolean returnBook(int userId, String isbn) {
        BorrowRecord record = Library.getBorrowRecords().stream()
                .filter(r -> r.getUserId() == userId && r.getIsbn().equals(isbn))
                .findFirst().orElse(null);

        if (record == null) {
            System.out.println("Không tìm thấy sách đã mượn.");
            return false;
        }

        Library.removeBorrowRecord(record);
        System.out.println("Trả sách thành công.");
        return true;
    }
}
