module cee.ceeplanner {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;

    opens cee.ceeplanner to javafx.fxml;
    exports cee.ceeplanner;
    exports cee.ceeplanner.controller;
    opens cee.ceeplanner.controller to javafx.fxml;
}