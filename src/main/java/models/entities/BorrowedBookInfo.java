package models.entities;

import java.sql.Date;

public class BorrowedBookInfo {
    private Document document;
    private BorrowRecord borrowRecord;

    public BorrowedBookInfo(Document document, BorrowRecord borrowRecord) {
        this.document = document;
        this.borrowRecord = borrowRecord;
    }

    public BorrowedBookInfo() {

    }

    public Document getDocument() {
        return document;
    }

    public BorrowRecord getBorrowRecord() {
        return borrowRecord;
    }

    public Date getBorrowDate() {
        return borrowRecord.getBorrowDate();
    }

    public void setBorrowRecord(BorrowRecord rec1) {
    }

    public void setDocument(Document doc1) {

    }
}
