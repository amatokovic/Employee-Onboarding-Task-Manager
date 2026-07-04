package org.example.taskmanager.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import org.example.taskmanager.entity.Task;
import org.example.taskmanager.enums.Priority;
import org.example.taskmanager.exception.DateOutOfRangeException;
import org.example.taskmanager.main.HelloApplication;
import org.example.taskmanager.record.TaskRecord;
import org.example.taskmanager.repository.Database;
import org.example.taskmanager.repository.TaskRepository;
import org.example.taskmanager.serialization.ChangeLogEntry;
import org.example.taskmanager.serialization.ChangeLogManager;

import java.util.Arrays;

/**
 * Controller class for adding new tasks in the admin interface of the task manager application.
 * Manages the UI and logic for creating and saving new task records.
 */
public class AdminTaskAddController {
    @FXML
    private TextField nameTextField;
    
    @FXML
    private TextField descriptionTextField;

    @FXML
    private ComboBox<String> typeComboBox;

    @FXML
    private ComboBox<String> priorityComboBox;

    @FXML
    private DatePicker deadlineDatePicker;

    @FXML
    private ComboBox<String> statusComboBox;

    TaskRepository taskRepository = new TaskRepository(Database.getConnection());

    /**
     * Initializes the controller.
     * Populates the type, priority, and status combo boxes with available options.
     */
    public void initialize() {
        typeComboBox.getItems().addAll(taskRepository.findAll().stream()
                .map(Task::getTaskRecord)
                .map(TaskRecord::type)
                .distinct()
                .toList());

        priorityComboBox.setItems(FXCollections.observableArrayList(
                Arrays.stream(Priority.values())
                        .map(Priority::getName)
                        .toList()
        ));

        statusComboBox.setItems(FXCollections.observableArrayList("Pending", "In Progress", "Completed"));
    }

    /**
     * Handles the process of adding a new task to the system.
     * Validates input, creates a new Task object, validates the task,
     * saves it to the database, logs the change, and displays appropriate alerts.
     * Clears the input fields after successful addition.
     */
    public void addTask() {
        if (nameTextField.getText().isEmpty() || descriptionTextField.getText().isEmpty()
                || typeComboBox.getValue() == null || priorityComboBox.getValue() == null
                || deadlineDatePicker.getValue() == null || statusComboBox.getValue() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Please fill in all the fields.");
            alert.showAndWait();
            return;
        }

        Task task = new Task.TaskBuilder()
                .setTaskRecord(new TaskRecord(
                                nameTextField.getText(),
                                descriptionTextField.getText(),
                                typeComboBox.getValue(),
                                Priority.valueOf(priorityComboBox.getValue())))
                .setDeadline(deadlineDatePicker.getValue().atStartOfDay())
                .setStatus(statusComboBox.getValue())
                .build();

        try {
            task.validate();
        } catch (DateOutOfRangeException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Deadline cannot be in the past ot today!");
            alert.showAndWait();
            return;
        }
        taskRepository.save(task);

        ChangeLogEntry entry = new ChangeLogEntry("Added a new task",
                "N/A",
                task.toString(),
                HelloApplication.getLoggedInUser());
        ChangeLogManager.logChange(entry);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Task added");
        alert.setHeaderText("The task was successfully added to the database!");
        alert.showAndWait();

        nameTextField.clear();
        descriptionTextField.clear();
        typeComboBox.getItems().clear();
        priorityComboBox.getItems().clear();
        deadlineDatePicker.setValue(null);
        statusComboBox.getItems().clear();
    }
}
