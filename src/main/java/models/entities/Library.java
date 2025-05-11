package models.entities;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@code Library} class serves as a central repository for in-memory
 * data including users, documents, borrow records, and reviews.
 * <p>
 * All collections are static, representing a singleton-style data store
 * shared across the application.
 */
public class Library {

    /**
     * List of all users in the system.
     */
    private static List<User> userList;

    /**
     * List of all documents (books, articles, etc.) available in the library.
     */
    private static List<Document> documentList;

    /**
     * List of all borrow records, indicating which user borrowed which document.
     */
    private static List<BorrowRecord> borrowRecords = new ArrayList<>();

    /**
     * List of all reviews made by users on documents.
     */
    private static List<Review> reviewList = new ArrayList<>();

    /**
     * Returns the list of all users in the library.
     *
     * @return the list of users
     */
    public static List<User> getUserList() {
        return userList;
    }

    /**
     * Sets the list of users in the library.
     *
     * @param userList the list of users to be set
     */
    public static void setUserList(List<User> userList) {
        Library.userList = userList;
    }

    /**
     * Returns the list of documents available in the library.
     *
     * @return the list of documents
     */
    public static List<Document> getDocumentList() {
        return documentList;
    }

    /**
     * Sets the list of documents in the library.
     *
     * @param documentList the list of documents to be set
     */
    public static void setDocumentList(List<Document> documentList) {
        Library.documentList = documentList;
    }

    /**
     * Returns the list of borrow records.
     *
     * @return the list of borrow records
     */
    public static List<BorrowRecord> getBorrowRecords() {
        return borrowRecords;
    }

    /**
     * Adds a borrow record to the list.
     *
     * @param record the borrow record to add
     */
    public static void addBorrowRecord(BorrowRecord record) {
        borrowRecords.add(record);
    }

    /**
     * Removes a borrow record from the list.
     *
     * @param record the borrow record to remove
     */
    public static void removeBorrowRecord(BorrowRecord record) {
        borrowRecords.remove(record);
    }

    /**
     * Returns the list of reviews.
     *
     * @return the list of reviews
     */
    public static List<Review> getReviewList() {
        return reviewList;
    }

    /**
     * Sets the list of reviews.
     *
     * @param reviews the list of reviews to be set
     */
    public static void setReviewList(List<Review> reviews) {
        reviewList = reviews;
    }
}
