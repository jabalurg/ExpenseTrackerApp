module com.example.expensetracker {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.expensetracker to javafx.fxml;
    exports com.example.expensetracker;
}