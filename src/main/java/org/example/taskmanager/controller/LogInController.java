package org.example.taskmanager.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.example.taskmanager.entity.LoginManager;
import org.example.taskmanager.exception.InvalidCredentialsException;
import org.example.taskmanager.main.HelloApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.example.taskmanager.controller.AdminMenuController.showNewScreen;

/**
 * Controller class for the login screen of the task manager application.
 * Handles user authentication and navigation to appropriate screens based on user role.
 */
public class LogInController {
    @FXML
    private TextField usernameTextField;

    @FXML
    private PasswordField passwordField;

    private static final Logger logger = LoggerFactory.getLogger(LogInController.class);

    /**
     * Handles the login process when the login button is clicked.
     * Validates user input, authenticates the user, and navigates to the appropriate screen.
     * Displays error alerts for empty fields or invalid credentials.
     */
    public void login() {
        if (usernameTextField.getText().isEmpty() || passwordField.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Please enter your username and password!");
            alert.showAndWait();
            return;
        }

        try {
            boolean isAuthenticated = LoginManager.authenticate(usernameTextField.getText(), passwordField.getText());

            if (usernameTextField.getText().equals("admin") && isAuthenticated) {
                showNewScreen("/org/example/taskmanager/adminWelcome.fxml");
                HelloApplication.setLoggedInUser(usernameTextField.getText());
            } else if (isAuthenticated) {
                showNewScreen("/org/example/taskmanager/userWelcome.fxml");
                HelloApplication.setLoggedInUser(usernameTextField.getText());
            }
        } catch (InvalidCredentialsException e) {
            logger.error("Invalid username or password", e);

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Invalid Credentials");
            alert.setHeaderText("You entered invalid username or password");
            alert.showAndWait();
        }
    }
}