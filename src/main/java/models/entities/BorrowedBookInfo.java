package models.entities;

import java.sql.Date;

/**
 * Lớp kết hợp thông tin của một tài liệu (Document) và bản ghi mượn tương ứng (BorrowRecord).
 * Dùng để hiển thị hoặc xử lý thông tin sách đã được mượn bởi người dùng.
 */
public class BorrowedBookInfo {
    private Document document;
    private BorrowRecord borrowRecord;

    /**
     * Tạo một đối tượng BorrowedBookInfo mới với đầy đủ thông tin.
     *
     * @param document     Thông tin sách
     * @param borrowRecord Thông tin mượn sách
     */
    public BorrowedBookInfo(Document document, BorrowRecord borrowRecord) {
        this.document = document;
        this.borrowRecord = borrowRecord;
    }

    /**
     * Constructor mặc định.
     */
    public BorrowedBookInfo() {
    }

    /**
     * Lấy thông tin sách.
     *
     * @return Đối tượng Document
     */
    public Document getDocument() {
        return document;
    }

    /**
     * Lấy thông tin bản ghi mượn.
     *
     * @return Đối tượng BorrowRecord
     */
    public BorrowRecord getBorrowRecord() {
        return borrowRecord;
    }

    /**
     * Lấy ngày mượn sách từ bản ghi mượn.
     *
     * @return Ngày mượn (java.sql.Date), hoặc null nếu borrowRecord là null
     */
    public Date getBorrowDate() {
        return borrowRecord.getBorrowDate();
    }

    /**
     * Cập nhật thông tin sách.
     *
     * @param doc1 Tài liệu cần thiết lập
     */
    public void setDocument(Document doc1) {

    }
}
