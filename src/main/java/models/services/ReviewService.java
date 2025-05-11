package models.services;

import models.dao.ReviewDAO;
import models.entities.Review;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Cung cấp các chức năng nghiệp vụ liên quan đến đánh giá (review),
 * như lấy, thêm, cập nhật và xoá đánh giá.
 */
public class ReviewService {

    /**
     * Lấy tất cả các đánh giá hiện có từ bộ nhớ.
     *
     * @return Danh sách tất cả các đánh giá.
     */
    public List<Review> getAllReviews() {
        return ReviewDAO.getAllReviewsFromMemory();
    }

    /**
     * Lấy danh sách đánh giá cho một tài liệu cụ thể theo ISBN.
     *
     * @param isbn Mã ISBN của tài liệu.
     * @return Danh sách các đánh giá liên quan đến ISBN đó.
     */
    public List<Review> getReviewsByIsbn(String isbn) {
        return getAllReviews().stream()
                .filter(review -> review.getDocumentIsbn().equalsIgnoreCase(isbn))
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách đánh giá của một người dùng cụ thể.
     *
     * @param userId ID của người dùng.
     * @return Danh sách các đánh giá được người dùng thực hiện.
     */
    public List<Review> getReviewsByUser(int userId) {
        return getAllReviews().stream()
                .filter(review -> review.getUserId() == userId)
                .collect(Collectors.toList());
    }


    /**
     * Thêm một đánh giá mới vào hệ thống.
     *
     * @param review Đánh giá cần thêm.
     */
    public void addReview(Review review) {
        ReviewDAO.addReview(review);
    }

    /**
     * Cập nhật một đánh giá hiện có.
     *
     * @param review Đánh giá đã chỉnh sửa.
     */
    public void updateReview(Review review) {
        // Gọi phương thức DAO để cập nhật review
        ReviewDAO.updateReview(review);
    }

    /**
     * Xoá một đánh giá dựa trên ID người dùng và ISBN tài liệu.
     *
     * @param userId ID người dùng.
     * @param documentIsbn ISBN của tài liệu.
     */
    public void deleteReview(int userId, String documentIsbn) {
        // Gọi phương thức DAO để xoá review
        ReviewDAO.deleteReview(userId, documentIsbn);
    }
}
