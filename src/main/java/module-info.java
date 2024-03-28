module com.ass3.assignment3_fsad {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.ass3.assignment3_fsad to javafx.fxml;
    exports com.ass3.assignment3_fsad;
}