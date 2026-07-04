package org.example.taskmanager.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.taskmanager.main.HelloApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Controller class for the admin menu in the task manager application.
 * Handles navigation to various admin screens and functionalities.
 */
public class AdminMenuController {
    private static final Logger logger = LoggerFactory.getLogger(AdminMenuController.class);

    /**
     * Navigates to the task search screen.
     */
    public void showTaskSearchScreen() {
        showNewScreen("/org/example/taskmanager/adminTaskSearch.fxml");
    }

    /**
     * Navigates to the task add screen.
     */
    public void showTaskAddScreen() {
        showNewScreen("/org/example/taskmanager/adminTaskAdd.fxml");
    }

    /**
     * Navigates to the task update screen.
     */
    public void showTaskUpdateScreen() {
        showNewScreen("/org/example/taskmanager/adminTaskUpdate.fxml");
    }

    /**
     * Navigates to the position search screen.
     */
    public void showPositionSearchScreen() {
        showNewScreen("/org/example/taskmanager/adminPositionSearch.fxml");
    }

    /**
     * Navigates to the position add screen.
     */
    public void showPositionAddScreen() {
        showNewScreen("/org/example/taskmanager/adminPositionAdd.fxml");
    }

    /**
     * Navigates to the employee search screen.
     */
    public void showEmployeeSearchScreen() {
        showNewScreen("/org/example/taskmanager/adminEmployeeSearch.fxml");
    }

    /**
     * Navigates to the employee add screen.
     */
    public void showEmployeeAddScreen() {
        showNewScreen("/org/example/taskmanager/adminEmployeeAdd.fxml");
    }

    /**
     * Navigates to the employee update screen.
     */
    public void showEmployeeUpdateScreen() {
        showNewScreen("/org/example/taskmanager/adminEmployeeUpdate.fxml");
    }

    /**
     * Opens the change log screen in a new window.
     */
    public void showChangeLogScreen() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(
                    HelloApplication.class.getResource("/org/example/taskmanager/adminChanges.fxml"));

            Stage stage = new Stage();
            Scene scene = new Scene(fxmlLoader.load(), 900, 600);
            stage.setTitle("Change Log");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            logger.error("Failed to load FXML", e);
        }
    }

    /**
     * Logs out the current user and returns to the login screen.
     */
    public void logOut() {
        showNewScreen("/org/example/taskmanager/logIn.fxml");
    }

    /**
     * Utility method to load and display a new FXML screen.
     *
     * @param path the path to the FXML file
     */
    static void showNewScreen(String path) {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource(path));
        try {
            Scene scene = new Scene(fxmlLoader.load(), 900, 600);
            HelloApplication.getMainStage().setScene(scene);
            HelloApplication.getMainStage().show();
        } catch (IOException e) {
            logger.error("Failed to load FXML", e);
        }
    }
}
