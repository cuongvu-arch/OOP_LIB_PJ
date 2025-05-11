package models.dao;

import models.data.DatabaseConnection;
import models.entities.Document;
import models.entities.Library;
import models.entities.Review;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO class responsible for managing review data.
 * Includes CRUD operations and support for loading into memory and rating calculations.
 */
public class ReviewDAO {

    /**
     * Retrieves all reviews from the database.
     *
     * @param conn The database connection.
     * @return A list of {@link Review} objects.
     */
    public static List<Review> getAllReviews(Connection conn) {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT * FROM review";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int userId = rs.getInt("user_id");
                String isbn = rs.getString("document_isbn");
                int rating = rs.getInt("rating");
                String comment = rs.getString("comment");
                Timestamp timestamp = rs.getTimestamp("created_at");
                LocalDateTime createdAt = timestamp.toLocalDateTime();

                Review review = new Review(userId, isbn, rating, comment, createdAt);
                reviews.add(review);
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách review: " + e.getMessage());
        }

        return reviews;
    }

    /**
     * Loads all review data from the database and stores it in memory via {@link Library#setReviewList(List)}.
     */
    public static void loadReviewData() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            List<Review> reviews = getAllReviews(conn);
            Library.setReviewList(reviews);
            System.out.println("Dữ liệu đánh giá đã được tải vào bộ nhớ.");
        } catch (Exception e) {
            System.err.println("Lỗi khi tải dữ liệu đánh giá: " + e.getMessage());
        }
    }

    /**
     * Retrieves all reviews currently loaded in memory.
     *
     * @return A list of {@link Review} objects.
     */
    public static List<Review> getAllReviewsFromMemory() {
        return Library.getReviewList();
    }

    /**
     * Inserts a new review into the database.
     *
     * @param review The {@link Review} object to be added.
     */
    public static void addReview(Review review) {
        String sql = "INSERT INTO review(user_id, document_isbn, rating, comment, created_at) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, review.getUserId());
            stmt.setString(2, review.getDocumentIsbn());
            stmt.setInt(3, review.getRating());
            stmt.setString(4, review.getComment());
            stmt.setTimestamp(5, Timestamp.valueOf(review.getCreatedAt()));

            stmt.executeUpdate();
            System.out.println("Đánh giá đã được thêm thành công.");
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm đánh giá: " + e.getMessage());
        }
    }

    /**
     * Updates an existing review in the database.
     *
     * @param review The {@link Review} object containing updated information.
     */
    public static void updateReview(Review review) {
        String sql = "UPDATE review SET rating = ?, comment = ? WHERE user_id = ? AND document_isbn = ? AND created_at = ?";

        // Lấy createdAt từ database (truy vấn review trước)
        LocalDateTime createdAt = getCreatedAtFromDatabase(review.getUserId(), review.getDocumentIsbn());

        if (createdAt == null) {
            System.out.println("Review không tồn tại để cập nhật.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, review.getRating());
            stmt.setString(2, review.getComment());
            stmt.setInt(3, review.getUserId());
            stmt.setString(4, review.getDocumentIsbn());
            stmt.setTimestamp(5, Timestamp.valueOf(createdAt));

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Review đã được cập nhật.");
            } else {
                System.out.println("Không tìm thấy review để cập nhật.");
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật review: " + e.getMessage());
        }
    }


    /**
     * Retrieves the createdAt timestamp for a review by user and document.
     *
     * @param userId        The user ID.
     * @param documentIsbn  The document ISBN.
     * @return The {@link LocalDateTime} of the review creation, or null if not found.
     */
    private static LocalDateTime getCreatedAtFromDatabase(int userId, String documentIsbn) {
        String sql = "SELECT created_at FROM review WHERE user_id = ? AND document_isbn = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, documentIsbn);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getTimestamp("created_at").toLocalDateTime();
                }
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy createdAt: " + e.getMessage());
        }

        return null;
    }

    /**
     * Deletes a review from the database using user ID, document ISBN, and creation time.
     *
     * @param userId       The user ID of the review.
     * @param documentIsbn The document ISBN of the review.
     */
    public static void deleteReview(int userId, String documentIsbn) {
        // Lấy createdAt từ database trước khi xoá review
        LocalDateTime createdAt = getCreatedAtFromDatabase(userId, documentIsbn);

        if (createdAt == null) {
            System.out.println("Review không tồn tại để xoá.");
            return;
        }

        // SQL để xoá review với điều kiện chính là userId, documentIsbn và createdAt
        String sql = "DELETE FROM review WHERE user_id = ? AND document_isbn = ? AND created_at = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Set các tham số cần thiết
            stmt.setInt(1, userId);  // Set userId
            stmt.setString(2, documentIsbn);  // Set documentIsbn
            stmt.setTimestamp(3, Timestamp.valueOf(createdAt));  // Set createdAt

            // Thực thi câu lệnh delete
            int rowsDeleted = stmt.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("Review đã được xoá.");
            } else {
                System.out.println("Không tìm thấy review để xoá.");
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi xoá review: " + e.getMessage());
        }
    }

    /**
     * Calculates the average rating of a document based on its ISBN.
     *
     * @param isbn The ISBN of the document.
     * @return The average rating as a double.
     */
    public static double calculateAverageRating(String isbn) {
        double avgRating = 0;
        int totalRating = 0;
        int count = 0;

        String sql = """
                SELECT rating
                FROM review
                WHERE document_isbn = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, isbn);
            ResultSet rs = stmt.executeQuery();

            // Tính tổng số điểm và số lượng đánh giá
            while (rs.next()) {
                totalRating += rs.getInt("rating");
                count++;
            }

            // Nếu có đánh giá thì tính điểm trung bình
            if (count > 0) {
                avgRating = (double) totalRating / count;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return avgRating;
    }

    /**
     * Retrieves the top N documents with the highest average ratings.
     *
     * @param topN Number of top-rated documents to retrieve.
     * @return A list of {@link Document} objects with title, ISBN, and average rating.
     */
    public List<Document> getTopRatedDocuments(int topN) {
        List<Document> result = new ArrayList<>();

        String sql = """
                SELECT d.isbn, d.title, AVG(r.rating) AS avg_rating
                FROM books d
                JOIN review r ON d.isbn = r.document_isbn
                GROUP BY d.isbn, d.title
                HAVING AVG(r.rating) > 0
                ORDER BY avg_rating DESC
                LIMIT ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, topN);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String isbn = rs.getString("isbn");
                String title = rs.getString("title");
                double avgRating = rs.getDouble("avg_rating");

                Document doc = new Document(isbn, title);
                doc.setAvgRating(avgRating);
                result.add(doc);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

}
