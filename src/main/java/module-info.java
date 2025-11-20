module library.main.library {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;

    opens library.main.library to javafx.fxml;
    exports library.main.library;
}
