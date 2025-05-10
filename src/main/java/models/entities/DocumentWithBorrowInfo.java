package models.entities;

public class DocumentWithBorrowInfo extends Document {
    private int totalQuantity;
    private int currentlyBorrowed;
    private int availableQuantity;

    public DocumentWithBorrowInfo(String isbn, String title, String thumbnailUrl, int totalQuantity, int currentlyBorrowed,int availableQuantity) {
        super(isbn, title, thumbnailUrl);
        this.totalQuantity = totalQuantity;
        this.currentlyBorrowed = currentlyBorrowed;
        this.availableQuantity = availableQuantity;

    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public int getCurrentlyBorrowed() {
        return currentlyBorrowed;
    }

    public int getAvailableQuantity() {
        return totalQuantity - currentlyBorrowed;
    }
}
