package org.example.taskmanager.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import org.example.taskmanager.entity.*;
import org.example.taskmanager.exception.DateOutOfRangeException;
import org.example.taskmanager.exception.PositionNotFoundException;
import org.example.taskmanager.main.HelloApplication;
import org.example.taskmanager.record.EmployeeRecord;
import org.example.taskmanager.repository.Database;
import org.example.taskmanager.repository.EmployeeRepository;
import org.example.taskmanager.repository.PositionRepository;
import org.example.taskmanager.repository.TaskRepository;
import org.example.taskmanager.serialization.ChangeLogEntry;
import org.example.taskmanager.serialization.ChangeLogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.example.taskmanager.entity.Employee.validateEmail;
import static org.example.taskmanager.enums.Messages.ERROR_ALERT;

/**
 * Controller class for adding new employees in the admin interface of the task manager application.
 * Manages the UI and logic for creating and saving new employee records.
 */
public final class AdminEmployeeAddController {
    @FXML
    private TextField usernameTextField;

    @FXML
    private TextField passwordPasswordField;

    @FXML
    private TextField firstNameTextField;

    @FXML
    private TextField lastNameTextField;

    @FXML
    private DatePicker startDateDatePicker;

    @FXML
    private TextField emailTextField;

    @FXML
    private ComboBox<String> positionComboBox;

    @FXML
    private ComboBox<String> tasksComboBox;

    EmployeeRepository employeeRepository = new EmployeeRepository(Database.getConnection());
    TaskRepository taskRepository = new TaskRepository(Database.getConnection());
    PositionRepository positionRepository = new PositionRepository(Database.getConnection());
    private final List<Task> selectedTasks = new ArrayList<>();

    private static final Logger logger = LoggerFactory.getLogger(AdminEmployeeAddController.class);

    /**
     * Initializes the controller.
     * Populates the position and task combo boxes with available options.
     */
    public void initialize() {
        positionComboBox.getItems().addAll(positionRepository.findAll().stream()
                .map(Position::getName)
                        .sorted(Comparator.reverseOrder())
                .toList());

        tasksComboBox.getItems().addAll(taskRepository.findAll().stream()
                .map(task -> task.getTaskRecord().name())
                .toList());
    }

    /**
     * Adds the selected task to the list of tasks for the new employee.
     * Displays appropriate alerts based on the success or failure of the operation.
     */
    public void addSelectedTask() {
        Optional<Task> selectedTask = taskRepository.findAll().stream()
                .filter(task -> task.getTaskRecord().name().equals(tasksComboBox.getValue()))
                .findFirst();

        if (selectedTask.isEmpty() || selectedTasks.contains(selectedTask.get())) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Failed to add a new task to the employee!");
            alert.setHeaderText("The task has already been added to the employee or is not selected!");
            alert.showAndWait();
        } else {
            selectedTasks.add(selectedTask.get());

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Successfully added a new task to the employee!");
            alert.setHeaderText("Task has been added to the employee!");
            alert.showAndWait();
        }
    }

    /**
     * Handles the process of adding a new employee to the system.
     * Validates input, creates a new Employee object, saves it to the database,
     * adds the user credentials, logs the change, and displays appropriate alerts.
     * Clears the input fields after successful addition.
     */
    public void addEmployee() {
        if (usernameTextField.getText().isEmpty() || passwordPasswordField.getText().isEmpty()
                || firstNameTextField.getText().isEmpty() || lastNameTextField.getText().isEmpty()
                || startDateDatePicker.getValue() == null || emailTextField.getText().isEmpty()
                || positionComboBox.getValue() == null || tasksComboBox.getValue() == null || selectedTasks.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(ERROR_ALERT.getMessage());
            alert.setHeaderText("Please fill in all the fields.");
            alert.showAndWait();
            return;
        } else if (!validateEmail(emailTextField.getText())) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(ERROR_ALERT.getMessage());
            alert.setHeaderText("Entered email in not valid!");
            alert.showAndWait();
            return;
        }

        try {
            String selectedPosition = positionComboBox.getValue();
            if (selectedPosition == null || selectedPosition.isEmpty()) {
                throw new PositionNotFoundException("Please select a position.");
            }

            Position position = positionRepository.getByName(selectedPosition)
                    .orElseThrow(() -> new PositionNotFoundException(
                            "Position not found for: " + selectedPosition));

            Employee employee = new Employee.EmployeeBuilder()
                    .setUsername(usernameTextField.getText())
                    .setPassword(LoginManager.hashPassword(passwordPasswordField.getText()))
                    .setEmployeeRecord(new EmployeeRecord(
                            firstNameTextField.getText(),
                            lastNameTextField.getText(),
                            startDateDatePicker.getValue()))
                    .setEmail(emailTextField.getText())
                    .setPosition(position)
                    .setTasks(selectedTasks)
                    .build();

            try {
                employee.validate();
            } catch (DateOutOfRangeException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Start date cannot be in the future!");
                alert.showAndWait();
                return;
            }
            employeeRepository.save(employee);

            Pair<String, String> namePass = new Pair<>(usernameTextField.getText(), passwordPasswordField.getText());
            LoginManager.addUser(namePass);

            ChangeLogEntry entry = new ChangeLogEntry("Added a new employee",
                    "N/A",
                    employee.toString(),
                    HelloApplication.getLoggedInUser());
            ChangeLogManager.logChange(entry);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Employee added");
            alert.setHeaderText("The employee was successfully added to the database!");
            alert.showAndWait();

            usernameTextField.clear();
            passwordPasswordField.clear();
            firstNameTextField.clear();
            lastNameTextField.clear();
            startDateDatePicker.setValue(null);
            emailTextField.clear();
            positionComboBox.getItems().clear();
            tasksComboBox.getItems().clear();
        } catch (PositionNotFoundException e) {
            logger.error("Failed to add a new employee", e);

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(ERROR_ALERT.getMessage());
            alert.setHeaderText("Error: Selected Position Not Found");
            alert.showAndWait();
        }
    }
}