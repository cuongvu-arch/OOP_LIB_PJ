package models.dao;

import models.data.DatabaseConnection;
import models.entities.Document;
import models.entities.Library;
import models.entities.Review;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReviewDAOTest {

    private Review sampleReview;
    private LocalDateTime createdAt;

    @BeforeEach
    void setUp() throws SQLException {
        // Set environment to test to use H2
        System.setProperty("env", "test");

        createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS); // Cắt bớt nano-giây
        sampleReview = new Review(1, "1234567890", 5, "Great book!", createdAt);

        // Create schema for H2
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS review (" +
                            "user_id INT, " +
                            "document_isbn VARCHAR(13), " +
                            "rating INT, " +
                            "comment VARCHAR(255), " +
                            "created_at TIMESTAMP, " +
                            "PRIMARY KEY (user_id, document_isbn, created_at))"
            );
            // Xóa dữ liệu cũ để đảm bảo test sạch
            conn.createStatement().execute("TRUNCATE TABLE review");
        }
    }

    @Test
    void testGetAllReviews_Success() throws SQLException {
        // Arrange
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.createStatement().executeUpdate(
                    "INSERT INTO review (user_id, document_isbn, rating, comment, created_at) " +
                            "VALUES (1, '1234567890', 5, 'Great book!', '" + Timestamp.valueOf(createdAt) + "')"
            );

            // Act
            List<Review> reviews = ReviewDAO.getAllReviews(conn);

            // Assert
            assertEquals(1, reviews.size());
            Review review = reviews.get(0);
            assertEquals(1, review.getUserId());
            assertEquals("1234567890", review.getDocumentIsbn());
            assertEquals(5, review.getRating());
            assertEquals("Great book!", review.getComment());
            assertEquals(createdAt.truncatedTo(ChronoUnit.MILLIS),
                    review.getCreatedAt().truncatedTo(ChronoUnit.MILLIS));
        }
    }

    @Test
    void testGetAllReviews_Empty() throws SQLException {
        // Act
        try (Connection conn = DatabaseConnection.getConnection()) {
            List<Review> reviews = ReviewDAO.getAllReviews(conn);

            // Assert
            assertTrue(reviews.isEmpty());
        }
    }

    @Test
    void testAddReview_Success() throws SQLException {
        // Act
        ReviewDAO.addReview(sampleReview);

        // Assert
        try (Connection conn = DatabaseConnection.getConnection()) {
            List<Review> reviews = ReviewDAO.getAllReviews(conn);
            assertEquals(1, reviews.size());
            Review review = reviews.get(0);
            assertEquals(1, review.getUserId());
            assertEquals("1234567890", review.getDocumentIsbn());
            assertEquals(5, review.getRating());
            assertEquals("Great book!", review.getComment());
            assertEquals(createdAt.truncatedTo(ChronoUnit.MILLIS),
                    review.getCreatedAt().truncatedTo(ChronoUnit.MILLIS));
        }
    }

    @Test
    void testUpdateReview_Success() throws SQLException {
        // Arrange: Thêm review trước
        ReviewDAO.addReview(sampleReview);

        // Update review
        Review updatedReview = new Review(1, "1234567890", 4, "Updated comment", createdAt);
        ReviewDAO.updateReview(updatedReview);

        // Assert
        try (Connection conn = DatabaseConnection.getConnection()) {
            List<Review> reviews = ReviewDAO.getAllReviews(conn);
            assertEquals(1, reviews.size());
            Review review = reviews.get(0);
            assertEquals(4, review.getRating());
            assertEquals("Updated comment", review.getComment());
        }
    }

    @Test
    void testUpdateReview_NotFound() throws SQLException {
        // Act: Cố gắng cập nhật review không tồn tại
        ReviewDAO.updateReview(sampleReview);

        // Assert: Không có review nào được thêm
        try (Connection conn = DatabaseConnection.getConnection()) {
            List<Review> reviews = ReviewDAO.getAllReviews(conn);
            assertTrue(reviews.isEmpty());
        }
    }

    @Test
    void testDeleteReview_Success() throws SQLException {
        // Arrange: Thêm review trước
        ReviewDAO.addReview(sampleReview);

        // Act
        ReviewDAO.deleteReview(1, "1234567890");

        // Assert
        try (Connection conn = DatabaseConnection.getConnection()) {
            List<Review> reviews = ReviewDAO.getAllReviews(conn);
            assertTrue(reviews.isEmpty());
        }
    }

    @Test
    void testDeleteReview_NotFound() throws SQLException {
        // Act: Cố gắng xóa review không tồn tại
        ReviewDAO.deleteReview(1, "1234567890");

        // Assert: Không có lỗi, và không có review nào
        try (Connection conn = DatabaseConnection.getConnection()) {
            List<Review> reviews = ReviewDAO.getAllReviews(conn);
            assertTrue(reviews.isEmpty());
        }
    }

    @Test
    void testCalculateAverageRating_Success() throws SQLException {
        // Arrange
        LocalDateTime secondCreatedAt = createdAt.plusHours(1);
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.createStatement().executeUpdate(
                    "INSERT INTO review (user_id, document_isbn, rating, comment, created_at) " +
                            "VALUES (1, '1234567890', 4, 'Good', '" + Timestamp.valueOf(createdAt) + "'), " +
                            "(2, '1234567890', 5, 'Great', '" + Timestamp.valueOf(secondCreatedAt) + "')"
            );
        }

        // Act
        double avgRating = ReviewDAO.calculateAverageRating("1234567890");

        // Assert
        assertEquals(4.5, avgRating, 0.01);
    }

    @Test
    void testCalculateAverageRating_NoReviews() {
        // Act
        double avgRating = ReviewDAO.calculateAverageRating("1234567890");

        // Assert
        assertEquals(0.0, avgRating, 0.01);
    }

    @Test
    void testGetTopRatedDocuments_Success() throws SQLException {
        // Arrange: Tạo bảng books và thêm dữ liệu
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS books (" +
                            "isbn VARCHAR(13) PRIMARY KEY, " +
                            "title VARCHAR(255))"
            );
            conn.createStatement().executeUpdate(
                    "INSERT INTO books (isbn, title) VALUES ('1234567890', 'Test Book')"
            );
            conn.createStatement().executeUpdate(
                    "INSERT INTO review (user_id, document_isbn, rating, comment, created_at) " +
                            "VALUES (1, '1234567890', 5, 'Great book!', '" + Timestamp.valueOf(createdAt) + "')"
            );
        }

        // Act
        List<Document> documents = new ReviewDAO().getTopRatedDocuments(1);

        // Assert
        assertEquals(1, documents.size());
        Document doc = documents.get(0);
        assertEquals("1234567890", doc.getIsbn());
        assertEquals("Test Book", doc.getTitle());
        assertEquals(5.0, doc.getAvgRating(), 0.01);
    }
}