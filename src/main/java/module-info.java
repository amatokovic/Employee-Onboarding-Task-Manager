module org.example.taskmanager {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.slf4j;
    requires jdk.compiler;
    requires java.sql;
    requires com.h2database;


    exports org.example.taskmanager.main;
    opens org.example.taskmanager.main to javafx.fxml;
    exports org.example.taskmanager.controller;
    opens org.example.taskmanager.controller to javafx.fxml;
}