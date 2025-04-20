module java.oop_library_project {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.sql;
    requires jbcrypt;
    requires mysql.connector.j;
    requires java.net.http;
    requires org.json;
    requires java.desktop;

    opens app to javafx.fxml;
    exports app;

    opens Controller to javafx.fxml;
    exports Controller;

    opens models to javafx.fxml;
    exports models;
    exports Controller.Book;
    opens Controller.Book to javafx.fxml;
    exports models.dao;
    opens models.dao to javafx.fxml;
    exports models.entities;
    opens models.entities to javafx.fxml;
    exports models.data;
    opens models.data to javafx.fxml;
}