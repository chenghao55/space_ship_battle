module com.binge.space_ship {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.binge.GameProject to javafx.fxml;
    exports com.binge.GameProject;
}
