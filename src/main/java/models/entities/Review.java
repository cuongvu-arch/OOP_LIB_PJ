package models.entities;

import java.time.LocalDateTime;

public class Review {
    private int userId;
    private String documentIsbn;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;

    public Review(int userId, String documentIsbn, int rating, String comment, LocalDateTime createdAt) {
        this.userId = userId;
        this.documentIsbn = documentIsbn;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    // Getters và Setters
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getDocumentIsbn() {
        return documentIsbn;
    }

    public void setDocumentIsbn(String documentIsbn) {
        this.documentIsbn = documentIsbn;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
