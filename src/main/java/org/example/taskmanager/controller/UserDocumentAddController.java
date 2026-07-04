package org.example.taskmanager.controller;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import org.example.taskmanager.entity.Employee;
import org.example.taskmanager.main.HelloApplication;
import org.example.taskmanager.repository.Database;
import org.example.taskmanager.repository.EmployeeRepository;
import org.example.taskmanager.serialization.ChangeLogEntry;
import org.example.taskmanager.serialization.ChangeLogManager;

import java.io.File;
import java.util.Optional;

/**
 * Controller class for adding documents to a user's profile in the task manager application.
 * This utility class provides a static method for document addition.
 */
public class UserDocumentAddController {
    static EmployeeRepository employeeRepository = new EmployeeRepository(Database.getConnection());

    private UserDocumentAddController() { }

    /**
     * Controller class for adding documents to a user's profile in the task manager application.
     * This utility class provides a static method for document addition.
     */
    public static void addDocument() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Add Document");
        alert.setHeaderText("Do you want to add a document?");
        alert.setContentText("Choose the next step: ");

        ButtonType chooseButton = new ButtonType("Choose");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(chooseButton, cancelButton);
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == chooseButton) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All files", "*.*"));

            File selectedFile = fileChooser.showOpenDialog(HelloApplication.getMainStage());

            if (selectedFile != null) {
                String filePath = selectedFile.getAbsolutePath();

                Optional<Employee> currentEmployee = employeeRepository.findAll().stream()
                        .filter(employee -> employee.getUsername().equals(HelloApplication.getLoggedInUser()))
                        .findFirst();

                if (currentEmployee.isPresent()){
                    Employee employee = currentEmployee.get();
                    employeeRepository.addDocumentToEmployee(employee.getId(), filePath);

                    ChangeLogEntry entry = new ChangeLogEntry(
                            "Added new document",
                            "N/A",
                            filePath,
                            HelloApplication.getLoggedInUser()
                    );
                    ChangeLogManager.logChange(entry);

                    alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Document added");
                    alert.setHeaderText("Document added successfully.");
                    alert.showAndWait();
                } else {
                    alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Employee not found");
                    alert.setContentText("Could not find an employee with the logged-in username.");
                    alert.showAndWait();
                }
            } else {
                alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("No File Selected");
                alert.setHeaderText("You haven't selected a file");
                alert.setContentText("Please select a file");
                alert.showAndWait();
            }
        }
    }
}
