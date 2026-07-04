package org.example.taskmanager.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import org.example.taskmanager.entity.Position;
import org.example.taskmanager.main.HelloApplication;
import org.example.taskmanager.repository.Database;
import org.example.taskmanager.repository.PositionRepository;
import org.example.taskmanager.serialization.ChangeLogEntry;
import org.example.taskmanager.serialization.ChangeLogManager;

/**
 * Controller class for adding new positions in the admin interface of the task manager application.
 * Manages the UI and logic for creating and saving new position records.
 */
public class AdminPositionAddController {
    @FXML
    private TextField nameTextField;

    @FXML
    private TextField descriptionTextField;

    PositionRepository positionRepository = new PositionRepository(Database.getConnection());

    /**
     * Handles the process of adding a new position to the system.
     * Validates input, creates a new Position object, saves it to the database,
     * logs the change, and displays appropriate alerts.
     * Clears the input fields after successful addition.
     */
    public void addPosition() {
        if (nameTextField.getText().isEmpty() || descriptionTextField.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Please fill in all the fields.");
            alert.showAndWait();
            return;
        }

        Position position = new Position.PositionBuilder()
                .setName(nameTextField.getText())
                .setDescription(descriptionTextField.getText())
                .build();

        positionRepository.save(position);

        ChangeLogEntry entry = new ChangeLogEntry("Added new position",
                "N/A",
                position.toString(),
                HelloApplication.getLoggedInUser());
        ChangeLogManager.logChange(entry);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Position added");
        alert.setHeaderText("The position was successfully added to the database!");
        alert.showAndWait();

        nameTextField.clear();
        descriptionTextField.clear();
    }
}
