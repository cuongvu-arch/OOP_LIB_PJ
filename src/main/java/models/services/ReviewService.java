package models.services;

import models.dao.ReviewDAO;
import models.entities.Review;

import java.util.List;
import java.util.stream.Collectors;

public class ReviewService {

    public List<Review> getAllReviews() {
        return ReviewDAO.getAllReviewsFromMemory();
    }

    public List<Review> getReviewsByIsbn(String isbn) {
        return getAllReviews().stream()
                .filter(review -> review.getDocumentIsbn().equalsIgnoreCase(isbn))
                .collect(Collectors.toList());
    }

    public List<Review> getReviewsByUser(int userId) {
        return getAllReviews().stream()
                .filter(review -> review.getUserId() == userId)
                .collect(Collectors.toList());
    }

    public void addReview(Review review) {
        ReviewDAO.addReview(review);
    }
}
