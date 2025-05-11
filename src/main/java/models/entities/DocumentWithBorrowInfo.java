package models.entities;

/**
 * Extends the {@link Document} class to include borrowing information
 * such as total quantity, number of books currently borrowed, and available quantity.
 */
public class DocumentWithBorrowInfo extends Document {

    /**
     * Total number of copies of the document in the library.
     */
    private int totalQuantity;

    /**
     * Number of copies currently borrowed by users.
     */
    private int currentlyBorrowed;

    /**
     * Number of copies available for borrowing.
     */
    private int availableQuantity;

    /**
     * Constructs a DocumentWithBorrowInfo with borrowing statistics.
     *
     * @param isbn              The ISBN of the document.
     * @param title             The title of the document.
     * @param thumbnailUrl      The URL of the thumbnail image.
     * @param totalQuantity     The total number of copies.
     * @param currentlyBorrowed The number of currently borrowed copies.
     * @param availableQuantity The number of available copies.
     */
    public DocumentWithBorrowInfo(String isbn, String title, String thumbnailUrl,
                                  int totalQuantity, int currentlyBorrowed, int availableQuantity) {
        super(isbn, title, thumbnailUrl);
        this.totalQuantity = totalQuantity;
        this.currentlyBorrowed = currentlyBorrowed;
        this.availableQuantity = availableQuantity;
    }

    /**
     * @return Total number of copies of this document.
     */
    public int getTotalQuantity() {
        return totalQuantity;
    }

    /**
     * @return Number of currently borrowed copies.
     */
    public int getCurrentlyBorrowed() {
        return currentlyBorrowed;
    }

    /**
     * @return Number of copies available for borrowing.
     */
    public int getAvailableQuantity() {
        return availableQuantity;
    }

    /**
     * @param totalQuantity Sets the total quantity of this document.
     */
    public void setTotalQuantity(int totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    /**
     * @param currentlyBorrowed Sets the number of currently borrowed copies.
     */
    public void setCurrentlyBorrowed(int currentlyBorrowed) {
        this.currentlyBorrowed = currentlyBorrowed;
    }

    /**
     * @param availableQuantity Sets the number of available copies.
     */
    public void setAvailableQuantity(int availableQuantity) {
        this.availableQuantity = availableQuantity;
    }
}
