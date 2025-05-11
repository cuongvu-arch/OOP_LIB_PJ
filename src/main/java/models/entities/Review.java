package models.entities;

import java.time.LocalDateTime;

/**
 * The {@code Review} class represents a user's review for a specific document.
 * It contains details such as rating, comment, and the time the review was created.
 */
public class Review {
    /**
     * The ID of the user who submitted the review.
     */
    private int userId;

    /**
     * The ISBN of the document being reviewed.
     */
    private String documentIsbn;

    /**
     * The rating given to the document (typically from 1 to 5).
     */
    private int rating;

    /**
     * The textual comment provided by the user.
     */
    private String comment;

    /**
     * The timestamp when the review was created.
     */
    private LocalDateTime createdAt;

    /**
     * Constructs a new {@code Review} with the specified details.
     *
     * @param userId       the ID of the user
     * @param documentIsbn the ISBN of the reviewed document
     * @param rating       the rating value
     * @param comment      the review comment
     * @param createdAt    the creation timestamp
     */
    public Review(int userId, String documentIsbn, int rating, String comment, LocalDateTime createdAt) {
        this.userId = userId;
        this.documentIsbn = documentIsbn;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    /**
     * Returns the user ID of the reviewer.
     *
     * @return the user ID
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Sets the user ID of the reviewer.
     *
     * @param userId the user ID
     */
    public void setUserId(int userId) {
        this.userId = userId;
    }

    /**
     * Returns the ISBN of the document being reviewed.
     *
     * @return the document ISBN
     */
    public String getDocumentIsbn() {
        return documentIsbn;
    }

    /**
     * Sets the ISBN of the document being reviewed.
     *
     * @param documentIsbn the document ISBN
     */
    public void setDocumentIsbn(String documentIsbn) {
        this.documentIsbn = documentIsbn;
    }

    /**
     * Returns the rating value.
     *
     * @return the rating
     */
    public int getRating() {
        return rating;
    }

    /**
     * Sets the rating value.
     *
     * @param rating the rating (e.g., 1 to 5)
     */
    public void setRating(int rating) {
        this.rating = rating;
    }

    /**
     * Returns the textual comment of the review.
     *
     * @return the comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * Sets the textual comment of the review.
     *
     * @param comment the comment
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Returns the timestamp when the review was created.
     *
     * @return the creation time
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the timestamp of when the review was created.
     *
     * @param createdAt the creation time
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
