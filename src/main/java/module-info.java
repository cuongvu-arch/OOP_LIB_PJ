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

    opens app to javafx.fxml;
    exports app;

    opens Controller to javafx.fxml;
    exports Controller;

    opens models to javafx.fxml;
    exports models;
}