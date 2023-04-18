module ca.centennialcollege.lab05ex01 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens ca.centennialcollege.lab05ex01 to javafx.fxml;
    exports ca.centennialcollege.lab05ex01;
}