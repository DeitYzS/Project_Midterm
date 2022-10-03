module archiver {
    requires javafx.controls;
    requires javafx.fxml;
    requires zip4j;

    opens archiver to javafx.fxml;
    opens archiver.controller to javafx.fxml;
    exports archiver;
    exports archiver.controller;
}