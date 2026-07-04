package org.example.taskmanager.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.Serializable;

/**
 * The main application class for the Task Manager.
 * This class initializes and launches the JavaFX application.
 */
public class HelloApplication extends Application implements Serializable {
    private static Stage mainStage;
    private static String loggedInUser;

    /**
     * Starts the JavaFX application.
     * This method is called automatically by the JavaFX runtime.
     *
     * @param stage the primary stage for this application
     * @throws IOException if the FXML file cannot be loaded
     */
    @Override
    public void start(Stage stage) throws IOException {
        mainStage = stage;

        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/org/example/taskmanager/logIn.fxml"));

        Scene scene = new Scene(fxmlLoader.load(), 900, 600);
        stage.setTitle("Login");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * The main entry point for the application.
     * This method launches the JavaFX application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        launch();
    }

    public static Stage getMainStage() {
        return mainStage;
    }

    public static void setLoggedInUser(String loggedInUser) { HelloApplication.loggedInUser = loggedInUser; }

    public static String getLoggedInUser() { return loggedInUser; }
}