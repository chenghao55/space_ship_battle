module com.binge.space_ship {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.desktop;

    opens com.binge.GameProject to javafx.fxml;
    exports com.binge.GameProject;
}
