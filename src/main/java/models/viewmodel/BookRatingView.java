package models.viewmodel;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import models.entities.Document;

public class BookRatingView {
    private final Document book;
    private final StringProperty title;
    private final StringProperty isbn;

    public BookRatingView(Document book) {
        this.book = book;
        this.title = new SimpleStringProperty(book.getTitle());
        this.isbn = new SimpleStringProperty(book.getIsbn());
    }

    public StringProperty titleProperty() {
        return title;
    }

    public StringProperty isbnProperty() {
        return isbn;
    }
}
