package models.entities;

/**
 * Represents a document or book entity in the library system.
 * Contains metadata such as title, author(s), ISBN, publisher, and more.
 */
public class Document {

    private String isbn;
    private String title;
    private String[] authors;
    private String publisher;
    private String publishedDate;
    private String description;
    private String thumbnailUrl;
    private double avgRating;
    private int quantity;
    private String qrCodePath;
    private String googleBooksUrl;

    /**
     * Default constructor.
     */
    public Document() {
    }

    /**
     * Constructor for book search and display.
     *
     * @param title        The book title.
     * @param isbn         The ISBN.
     * @param thumbnailUrl The thumbnail image URL.
     */
    public Document(String title, String isbn, String thumbnailUrl) {
        this.title = title;
        this.isbn = isbn;
        this.thumbnailUrl = thumbnailUrl;
    }

    /**
     * Basic constructor with title and ISBN.
     *
     * @param isbn  The ISBN of the document.
     * @param title The title of the document.
     */
    public Document(String isbn, String title) {
        this.title = title;
        this.isbn = isbn;
    }

    /**
     * Full constructor with most book metadata.
     */
    public Document(String isbn, String title, String[] authors, String publisher,
                    String publishedDate, String description, String thumbnailUrl, String qrCodePath) {
        this.isbn = isbn;
        this.title = title;
        this.authors = authors;
        this.publisher = publisher;
        this.publishedDate = publishedDate;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.qrCodePath = qrCodePath;
    }

    /**
     * Full constructor with additional Google Books URL and default quantity = 1.
     */
    public Document(String isbn, String title, String[] authors, String publisher,
                    String publishedDate, String description, String thumbnailUrl,
                    String qrCodePath, String googleBooksUrl) {
        this.isbn = isbn;
        this.title = title;
        this.authors = authors;
        this.publisher = publisher;
        this.publishedDate = publishedDate;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.qrCodePath = qrCodePath;
        this.googleBooksUrl = googleBooksUrl;
        this.quantity = 1;
    }

    // === Getters ===

    /**
     * @return ISBN of the document.
     */
    public String getIsbn() {
        return isbn;
    }

    /**
     * @return Title of the document.
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return Array of authors.
     */
    public String[] getAuthors() {
        return authors;
    }

    /**
     * @return Publisher of the document.
     */
    public String getPublisher() {
        return publisher;
    }

    /**
     * @return Published date (as String).
     */
    public String getPublishedDate() {
        return publishedDate;
    }

    /**
     * @return Description or summary of the document.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return URL to thumbnail image.
     */
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    /**
     * @return Average rating of the document.
     */
    public double getAvgRating() {
        return avgRating;
    }

    /**
     * @return Quantity available in library.
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * @return Path to QR code image.
     */
    public String getQrCodePath() {
        return qrCodePath;
    }

    /**
     * @return Google Books URL, if available.
     */
    public String getGoogleBooksUrl() {
        return googleBooksUrl;
    }

    // === Setters ===

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthors(String[] authors) {
        this.authors = authors;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public void setPublishedDate(String publishedDate) {
        this.publishedDate = publishedDate;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public void setAvgRating(double avgRating) {
        this.avgRating = avgRating;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setQrCodePath(String qrCodePath) {
        this.qrCodePath = qrCodePath;
    }

    public void setGoogleBooksUrl(String googleBooksUrl) {
        this.googleBooksUrl = googleBooksUrl;
    }

    /**
     * Returns a string representation of the document for display purposes.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ISBN: ").append(isbn).append("\n");
        sb.append("Tiêu đề: ").append(title).append("\n");
        sb.append("Tác giả: ");
        if (authors != null && authors.length > 0) {
            sb.append(String.join(", ", authors));
        } else {
            sb.append("N/A");
        }
        sb.append("\n");
        sb.append("Nhà xuất bản: ").append(publisher != null ? publisher : "N/A").append("\n");
        sb.append("Ngày xuất bản: ").append(publishedDate != null ? publishedDate : "N/A").append("\n");
        sb.append("Mô tả: ").append(description != null ? description : "N/A").append("\n");
        return sb.toString();
    }
}
