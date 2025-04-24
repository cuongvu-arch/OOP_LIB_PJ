package app;

import java.util.Arrays;

public class Document {

    private String isbn;
    private String title;
    private String[] authors;
    private String publisher;
    private String publishedDate;
    private String description;
    private String thumbnailUrl;

    public Document(String isbn, String title, String[] authors, String publisher, String publishedDate, String description, String thumbnailUrl) {
        this.isbn = isbn;
        this.title = title;
        this.authors = authors;
        this.publisher = publisher;
        this.publishedDate = publishedDate;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
    }

    // Getter & Setter
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