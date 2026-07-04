package org.example.taskmanager.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.taskmanager.entity.Task;
import org.example.taskmanager.enums.Priority;
import org.example.taskmanager.exception.InvalidIdFormatException;
import org.example.taskmanager.main.HelloApplication;
import org.example.taskmanager.record.TaskRecord;
import org.example.taskmanager.repository.Database;
import org.example.taskmanager.repository.TaskRepository;
import org.example.taskmanager.serialization.ChangeLogEntry;
import org.example.taskmanager.serialization.ChangeLogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.example.taskmanager.enums.Messages.ERROR_ALERT;

/**
 * Controller class for updating tasks in the admin interface of the task manager application.
 * Manages the UI and logic for modifying existing task records.
 */
public class AdminTaskUpdateController {
    @FXML
    private TextField taskIdTextField;

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

    private static final Logger logger = LoggerFactory.getLogger(AdminTaskUpdateController.class);

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
     * Handles the process of updating an existing task's information.
     * Validates input, updates the Task object, saves changes to the database,
     * logs the changes, and displays appropriate alerts.
     */
    public void updateTask() {
        Long id;

        if (taskIdTextField.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(ERROR_ALERT.getMessage());
            alert.setHeaderText("Please enter an ID");
            alert.showAndWait();
            return;
        }

        try {
            id = Long.parseLong(taskIdTextField.getText());
        } catch (InvalidIdFormatException | NumberFormatException e) {
            logger.error("User entered an incorrect format for ID", e);

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(ERROR_ALERT.getMessage());
            alert.setHeaderText("Please enter a valid ID");
            alert.showAndWait();
            return;
        }

        Optional<Task> optionalTask = taskRepository.findById(id);

        if (optionalTask.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(ERROR_ALERT.getMessage());
            alert.setHeaderText("Task with entered ID not found!");
            alert.showAndWait();
            return;
        }

        Task task = optionalTask.get();

        Map<String, Object> updates = new HashMap<>();

        if (!nameTextField.getText().isEmpty()) {
            updates.put("name", nameTextField.getText());
        }
        if (!descriptionTextField.getText().isEmpty()) {
            updates.put("description", descriptionTextField.getText());
        }
        if (typeComboBox.getValue() != null) {
            updates.put("type", typeComboBox.getValue());
        }
        if (priorityComboBox.getValue() != null) {
            updates.put("priority", priorityComboBox.getValue());
        }
        if (deadlineDatePicker.getValue() != null) {
            updates.put("deadline", deadlineDatePicker.getValue().atStartOfDay());
        }
        if (statusComboBox.getValue() != null) {
            updates.put("status", statusComboBox.getValue());
        }

        if (!updates.isEmpty() && confirmUpdate()) {
            updates.forEach((field, newValue) -> {
                Object oldValue = Optional.ofNullable(getOldValue(task, field)).orElse("N/A");

                ChangeLogManager.logChange(new ChangeLogEntry(
                        "Updated " + field + " of task with ID " + task.getId(),
                        oldValue.toString(),
                        newValue.toString(),
                        HelloApplication.getLoggedInUser()));
            });
            taskRepository.update(id, updates);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Task Updated");
            alert.setHeaderText("The task was successfully updated!");
            alert.showAndWait();
        }
    }

    /**
     * Displays a confirmation dialog for the update operation.
     *
     * @return true if the user confirms the update, false otherwise
     */
    private boolean confirmUpdate() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Edit Task");
        alert.setHeaderText("Are you sure you want to edit this task?");
        return alert.showAndWait().map(button -> button == ButtonType.OK).orElse(false);
    }

    /**
     * Retrieves the old value of a specific field from the Task object.
     *
     * @param task the Task object
     * @param field the field name to retrieve
     * @return the old value of the specified field
     */
    private Object getOldValue(Task task, String field) {
        return switch (field) {
            case "name" -> task.getTaskRecord().name();
            case "description" -> task.getTaskRecord().description();
            case "type" -> task.getTaskRecord().type();
            case "priority" -> task.getTaskRecord().priority();
            case "deadline" -> task.getDeadline();
            case "status" -> task.getStatus();
            default -> "N/A";
        };
    }
}
