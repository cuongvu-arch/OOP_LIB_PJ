package models.viewmodel;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import models.entities.Document;

public class BookRatingView {
    private final Document book;

    private final StringProperty title;
    private final StringProperty isbn;
    private final StringProperty thumbnailUrl;

    public BookRatingView(Document book) {
        this.book = book;
        this.title = new SimpleStringProperty(book.getTitle());
        this.isbn = new SimpleStringProperty(book.getIsbn());
        this.thumbnailUrl = new SimpleStringProperty(book.getThumbnailUrl());
    }

    public StringProperty titleProperty() { return title; }
    public StringProperty isbnProperty() { return isbn; }
    public StringProperty thumbnailUrlProperty() { return thumbnailUrl; }
    public Document getBook() {
        return book;
    }
}
