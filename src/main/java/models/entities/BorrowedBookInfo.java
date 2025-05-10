package models.entities;

public class BorrowedBookInfo {
    private Document document;
    private BorrowRecord borrowRecord;

    public BorrowedBookInfo(Document document, BorrowRecord borrowRecord) {
        this.document = document;
        this.borrowRecord = borrowRecord;
    }

    public Document getDocument() {
        return document;
    }

    public BorrowRecord getBorrowRecord() {
        return borrowRecord;
    }
}
