package models.services;

import models.dao.ReviewDAO;
import models.entities.Review;
import models.services.ReviewService;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ReviewServiceTest {

    @Test
    void testGetAllReviews_returnsCorrectList() {
        Review r1 = new Review(1, "isbn1", 5, "Great", null);
        Review r2 = new Review(2, "isbn2", 4, "Good", null);

        try (MockedStatic<ReviewDAO> mock = mockStatic(ReviewDAO.class)) {
            mock.when(ReviewDAO::getAllReviewsFromMemory).thenReturn(Arrays.asList(r1, r2));

            ReviewService service = new ReviewService();
            List<Review> reviews = service.getAllReviews();

            assertEquals(2, reviews.size());
            assertTrue(reviews.contains(r1));
            assertTrue(reviews.contains(r2));
        }
    }

    @Test
    void testGetReviewsByIsbn_filtersCorrectly() {
        Review r1 = new Review(1, "isbn1", 5, "Excellent", null);
        Review r2 = new Review(2, "isbn2", 3, "Okay", null);

        try (MockedStatic<ReviewDAO> mock = mockStatic(ReviewDAO.class)) {
            mock.when(ReviewDAO::getAllReviewsFromMemory).thenReturn(Arrays.asList(r1, r2));

            ReviewService service = new ReviewService();
            List<Review> result = service.getReviewsByIsbn("isbn1");

            assertEquals(1, result.size());
            assertEquals("isbn1", result.get(0).getDocumentIsbn());
        }
    }

    @Test
    void testGetReviewsByUser_filtersCorrectly() {
        Review r1 = new Review(1, "isbn1", 5, "Nice", null);
        Review r2 = new Review(2, "isbn2", 2, "Bad", null);

        try (MockedStatic<ReviewDAO> mock = mockStatic(ReviewDAO.class)) {
            mock.when(ReviewDAO::getAllReviewsFromMemory).thenReturn(Arrays.asList(r1, r2));

            ReviewService service = new ReviewService();
            List<Review> result = service.getReviewsByUser(1);

            assertEquals(1, result.size());
            assertEquals(1, result.get(0).getUserId());
        }
    }

    @Test
    void testAddReview_callsDAO() {
        Review review = new Review(3, "isbn3", 4, "Good book", null);

        try (MockedStatic<ReviewDAO> mock = mockStatic(ReviewDAO.class)) {
            ReviewService service = new ReviewService();
            service.addReview(review);

            mock.verify(() -> ReviewDAO.addReview(review), times(1));
        }
    }

    @Test
    void testUpdateReview_callsDAO() {
        Review review = new Review(1, "isbn1", 5, "Updated review", null);

        try (MockedStatic<ReviewDAO> mock = mockStatic(ReviewDAO.class)) {
            ReviewService service = new ReviewService();
            service.updateReview(review);

            mock.verify(() -> ReviewDAO.updateReview(review), times(1));
        }
    }

    @Test
    void testDeleteReview_callsDAO() {
        int userId = 1;
        String isbn = "isbn1";

        try (MockedStatic<ReviewDAO> mock = mockStatic(ReviewDAO.class)) {
            ReviewService service = new ReviewService();
            service.deleteReview(userId, isbn);

            mock.verify(() -> ReviewDAO.deleteReview(userId, isbn), times(1));
        }
    }
}

