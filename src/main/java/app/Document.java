package app;

import java.util.Arrays; // Import cần thiết cho Arrays.toString

public class Document {

    private String isbn; // <<< THÊM THUỘC TÍNH ISBN
    private String title;
    private String[] authors;
    private String publisher;
    private String publishedDate;
    private String description;

    // Constructor <<< CẬP NHẬT CONSTRUCTOR
    public Document(String isbn, String title, String[] authors, String publisher, String publishedDate, String description) {
        this.isbn = isbn; // <<< GÁN GIÁ TRỊ ISBN
        this.title = title;
        this.authors = authors;
        this.publisher = publisher;
        this.publishedDate = publishedDate;
        this.description = description;
    }

    // Getter & Setter
    public String getIsbn() { // <<< THÊM GETTER CHO ISBN
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

    // Hiển thị ra dạng chuỗi <<< CẬP NHẬT toString()
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ISBN: ").append(isbn).append("\n"); // <<< THÊM HIỂN THỊ ISBN
        sb.append("Tiêu đề: ").append(title).append("\n");
        sb.append("Tác giả: ");
        // Cách hiển thị mảng tác giả tốt hơn:
        if (authors != null && authors.length > 0) {
            sb.append(String.join(", ", authors));
        } else {
            sb.append("N/A"); // Hoặc để trống
        }
        sb.append("\n");
        sb.append("Nhà xuất bản: ").append(publisher != null ? publisher : "N/A").append("\n"); // Xử lý null
        sb.append("Ngày xuất bản: ").append(publishedDate != null ? publishedDate : "N/A").append("\n"); // Xử lý null
        sb.append("Mô tả: ").append(description != null ? description : "N/A").append("\n"); // Xử lý null
        return sb.toString();
    }
}