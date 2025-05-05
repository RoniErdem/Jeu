module com.example.demo2 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.desktop; // Important pour que JavaFX puisse accéder au Main
    requires java.prefs;

    opens com.example.demo2 to javafx.fxml;
    opens com.example.demo2.footgame to javafx.fxml, javafx.graphics;



    exports com.example.demo2;
    exports com.example.demo2.footgame;
}
