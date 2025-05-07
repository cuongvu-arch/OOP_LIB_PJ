package models.viewmodel;

import javafx.beans.property.SimpleStringProperty;

public class BookBorrowedView {
    private final SimpleStringProperty display;

    public BookBorrowedView(String title, String isbn) {
        this.display = new SimpleStringProperty(title + " (" + isbn + ")");
    }

    public String getDisplay() {
        return display.get();
    }

    public SimpleStringProperty displayProperty() {
        return display;
    }
}

