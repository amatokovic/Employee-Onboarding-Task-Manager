package org.example.taskmanager.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import org.example.taskmanager.entity.Employee;
import org.example.taskmanager.entity.LoginManager;
import org.example.taskmanager.entity.Position;
import org.example.taskmanager.exception.InvalidIdFormatException;
import org.example.taskmanager.main.HelloApplication;
import org.example.taskmanager.repository.Database;
import org.example.taskmanager.repository.EmployeeRepository;
import org.example.taskmanager.repository.PositionRepository;
import org.example.taskmanager.repository.TaskRepository;
import org.example.taskmanager.serialization.ChangeLogEntry;
import org.example.taskmanager.serialization.ChangeLogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.example.taskmanager.enums.Messages.ERROR_ALERT;

/**
 * Controller class for updating employee information in the admin interface of the task manager application.
 * Handles the UI and logic for modifying existing employee records.
 */
public class AdminEmployeeUpdateController {
    @FXML
    private TextField idTextField;

    @FXML
    private TextField usernameTextField;

    @FXML
    private TextField passwordTextField;

    @FXML
    private ComboBox<String> positionComboBox;

    @FXML
    private ComboBox<String> addTaskComboBox;

    EmployeeRepository employeeRepository = new EmployeeRepository(Database.getConnection());
    PositionRepository positionRepository = new PositionRepository(Database.getConnection());
    TaskRepository taskRepository = new TaskRepository(Database.getConnection());

    private static final Logger logger = LoggerFactory.getLogger(AdminEmployeeUpdateController.class);

    /**
     * Initializes the controller.
     * Populates the position and task combo boxes with available options.
     */
    public void initialize() {
        positionComboBox.getItems().addAll(positionRepository.findAll()
                .stream()
                .map(Position::getName).toList());
        addTaskComboBox.getItems().addAll(taskRepository.findAll()
                .stream()
                .map(task -> task.getTaskRecord().name()).toList());
    }

    /**
     * Handles the process of updating an existing employee's information.
     * Validates input, updates the Employee object, saves changes to the database,
     * updates user credentials if necessary, logs the changes, and displays appropriate alerts.
     */
    public void updateEmployee() {
        Long id;

        if (idTextField.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(ERROR_ALERT.getMessage());
            alert.setHeaderText("Please enter an ID");
            alert.showAndWait();
            return;
        }

        try {
            id = Long.parseLong(idTextField.getText());
        } catch (InvalidIdFormatException | NumberFormatException e) {
            logger.error("User entered an incorrect format for ID", e);

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(ERROR_ALERT.getMessage());
            alert.setHeaderText("Please enter a valid ID");
            alert.showAndWait();
            return;
        }

        Optional<Employee> optionalEmployee = employeeRepository.findById(id);

        if (optionalEmployee.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(ERROR_ALERT.getMessage());
            alert.setHeaderText("Employee with entered ID not found!");
            alert.showAndWait();
            return;
        }

        Employee employee = optionalEmployee.get();

        Map<String, Object> updates = new HashMap<>();

        if (!usernameTextField.getText().isEmpty()) {
            updates.put("username", usernameTextField.getText());
        }
        if (!passwordTextField.getText().isEmpty()) {
            updates.put("password", "N/A");
        }

        positionRepository.getByName(positionComboBox.getValue()).ifPresent(p -> updates.put("position", p.getId()));
        taskRepository.getByName(addTaskComboBox.getValue()).ifPresent(t -> {
            employeeRepository.addTaskToEmployee(id, t.getId());
            updates.put("task", t.getTaskRecord().name());
        });

        if (!updates.isEmpty() && confirmUpdate()) {
            updates.forEach((field, newValue) -> {
                Object oldValue = Optional.ofNullable(getOldValue(employee, field)).orElse("N/A");

                ChangeLogManager.logChange(new ChangeLogEntry(
                        "Updated " + field + " of employee with ID " + employee.getId(),
                        oldValue.toString(),
                        newValue.toString(),
                        HelloApplication.getLoggedInUser()));
            });
            employeeRepository.update(id, updates);

            LoginManager.updateUser(employee.getUsername(), usernameTextField.getText(), passwordTextField.getText());

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Employee Updated");
            alert.setHeaderText("The employee was successfully updated!");
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
        alert.setTitle("Edit Employee");
        alert.setHeaderText("Are you sure you want to edit this employee?");
        return alert.showAndWait().map(button -> button == ButtonType.OK).orElse(false);
    }

    /**
     * Retrieves the old value of a specific field from the Employee object.
     *
     * @param employee the Employee object
     * @param field the field name to retrieve
     * @return the old value of the specified field
     */
    private Object getOldValue(Employee employee, String field) {
        return switch (field) {
            case "username" -> employee.getUsername();
            case "password" -> employee.getPassword();
            case "position" -> employee.getPosition() != null ? employee.getPosition() : "N/A";
            case "task" -> employee.getTasks();
            default -> "N/A";
        };
    }
}