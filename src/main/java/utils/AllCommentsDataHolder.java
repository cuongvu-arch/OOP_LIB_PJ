package utils;

import models.entities.Document;
import models.entities.Review;

import java.util.List;

public class AllCommentsDataHolder {
    private static Document currentBook;
    private static List<Review> allReviews;

    public static void setCurrentBook(Document book) {
        currentBook = book;
    }

    public static Document getCurrentBook() {
        return currentBook;
    }

    public static void setAllReviews(List<Review> reviews) {
        allReviews = reviews;
    }

    public static List<Review> getAllReviews() {
        return allReviews;
    }

    public static void clear() {
        currentBook = null;
        allReviews = null;
    }
}
