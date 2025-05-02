package models.dao;

import models.data.DatabaseConnection;
import models.entities.Library;
import models.entities.Review;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReviewDAO {

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

    public static void loadReviewData() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            List<Review> reviews = getAllReviews(conn);
            Library.setReviewList(reviews);
            System.out.println("Dữ liệu đánh giá đã được tải vào bộ nhớ.");
        } catch (Exception e) {
            System.err.println("Lỗi khi tải dữ liệu đánh giá: " + e.getMessage());
        }
    }

    public static List<Review> getAllReviewsFromMemory() {
        return Library.getReviewList();
    }

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
}
