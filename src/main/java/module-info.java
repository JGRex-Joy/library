module library.main.library {
    requires javafx.controls;
    requires javafx.fxml;

    requires java.sql;

    opens library.main.library to javafx.fxml;
    exports library.main.library;
    exports library.main.library.controllers;
    opens library.main.library.controllers to javafx.fxml;
    exports library.main.library.models;
    opens library.main.library.models to javafx.fxml;
    exports library.main.library.database;
    opens library.main.library.database to javafx.fxml;
}
