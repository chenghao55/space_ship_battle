module com.binge.space_ship {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.binge.space_ship to javafx.fxml;
    exports com.binge.space_ship;

    opens com.binge.GameProject to javafx.fxml;
    exports com.binge.GameProject;
}