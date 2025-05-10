package models.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Library {
    private static List<User> userList;
    private static List<Document> documentList;
    private static List<BorrowRecord> borrowRecords = new ArrayList<>();
    private static List<Review> reviewList = new ArrayList<>();

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

    public static List<Review> getReviewList() {
        return reviewList;
    }

    public static void setReviewList(List<Review> reviews) {
        reviewList = reviews;
    }

}
