package models.entities;

public class Document {

    private String isbn;
    private String title;
    private String[] authors;
    private String publisher;
    private String thumbnailUrl;
    private double avgRating;
    private int quantity;
    private String qrCodePath;
    private String googleBooksUrl;
    private String description;
    private String publishedDate;

    public Document(String isbn, String title, String[] authors, String publisher, String publishedDate, String description, String thumbnailUrl, int quantity, String qrCodePath) {
        this.isbn = isbn;
        this.title = title;
        this.authors = authors;
        this.publisher = publisher;
        this.publishedDate = publishedDate;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.quantity = quantity;
        this.qrCodePath = qrCodePath;
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



    public Document(String isbn, String title, String[] authors, String publisher, String publishedDate, String description, String thumbnailUrl, String qrCodePath) {
        this.isbn = isbn;
        this.title = title;
        this.authors = authors;
        this.publisher = publisher;
        this.publishedDate = publishedDate;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.qrCodePath = qrCodePath;
    }
    public Document(String isbn, String title, String[] authors, String publisher, String publishedDate, String description, String thumbnailUrl) {
        this.isbn = isbn;
        this.title = title;
        this.authors = authors;
        this.publisher = publisher;
        this.publishedDate = publishedDate;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
    }

    public Document(String isbn, String title, String[] authors, String publisher, String publishedDate,
                    String description, String thumbnailUrl, String qrCodePath, String googleBooksUrl) {
        this.isbn = isbn;
        this.title = title;
        this.authors = authors;
        this.publisher = publisher;
        this.publishedDate = publishedDate;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.qrCodePath = qrCodePath;
        this.googleBooksUrl = googleBooksUrl;
        this.quantity = 1; // Giá trị mặc định
    }

    // Getter & Setter
    public Document(String title, String isbn, String thumbnailUrl) {
        this.title = title;
        this.isbn = isbn;
        this.thumbnailUrl = thumbnailUrl;
    }

    public Document(String isbn, String title) {
        this.title = title;
        this.isbn = isbn;
    }

    public Document(){}

    public double getAvgRating() {
        return avgRating;
    }

    public void setAvgRating(double avgRating) {
        this.avgRating = avgRating;
    }


    public String getIsbn() {
        return isbn;
    }

    public String getTitle() {
        return title;
    }

    public String[] getAuthors() {
        return authors;
    }

    public String getPublisher() {
        return publisher;
    }

    public String getPublishedDate() {
        return publishedDate;
    }

    public String getDescription() {
        return description;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getQrCodePath() {
        return qrCodePath;
    }

    public void setQrCodePath(String qrCodePath) {
        this.qrCodePath = qrCodePath;
    }

    public String getGoogleBooksUrl() {
        return googleBooksUrl;
    }

    public void setGoogleBooksUrl(String googleBooksUrl) {
        this.googleBooksUrl = googleBooksUrl;
    }

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

    public void setTitle(String s) {
        this.title = s;
    }

    public void setAuthors(String[] authors) {
        this.authors = authors;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }
}