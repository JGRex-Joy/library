module library.main.library {
    requires javafx.controls;
    requires javafx.fxml;

    requires java.sql;

    opens library.main.library to javafx.fxml;
    exports library.main.library;
}
