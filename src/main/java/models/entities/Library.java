package models.entities;

import java.util.ArrayList;
import java.util.List;

public class Library {
    private static List<User> userList;
    private static List<Document> documentList;
    private static List<BorrowRecord> borrowRecords = new ArrayList<>();

    public static List<User> getUserList() {
        return userList;
    }

    public static void setUserList(List<User> userList) {
        Library.userList = userList;
    }

    public static List<Document> getDocumentList() {
        return documentList;
    }

    public static void setDocumentList(List<Document> documentList) {
        Library.documentList = documentList;
    }

    public static List<BorrowRecord> getBorrowRecords() {
        return borrowRecords;
    }

    public static void addBorrowRecord(BorrowRecord record) {
        borrowRecords.add(record);
    }

    public static void removeBorrowRecord(BorrowRecord record) {
        borrowRecords.remove(record);
    }
}
