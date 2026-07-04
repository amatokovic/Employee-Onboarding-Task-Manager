package org.example.taskmanager.controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.taskmanager.entity.Employee;
import org.example.taskmanager.entity.LoginManager;
import org.example.taskmanager.entity.Position;
import org.example.taskmanager.exception.InvalidIdFormatException;
import org.example.taskmanager.main.HelloApplication;
import org.example.taskmanager.repository.Database;
import org.example.taskmanager.repository.EmployeeRepository;
import org.example.taskmanager.repository.PositionRepository;
import org.example.taskmanager.serialization.ChangeLogEntry;
import org.example.taskmanager.serialization.ChangeLogManager;
import org.example.taskmanager.thread.SortingEmployeesThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.example.taskmanager.enums.Messages.EMPLOYEE_REMOVAL_ALERT_TITLE;
import static org.example.taskmanager.enums.Messages.ERROR_ALERT;

/**
 * Controller class for searching and managing employees in the admin interface of the task manager application.
 * Handles employee search, filtering, and removal operations.
 */
public class AdminEmployeeSearchController {
    @FXML
    private TextField emailTextField;

    @FXML
    private ComboBox<String> positionComboBox;

    @FXML
    private TextField idTextField;

    @FXML
    private TableView<Employee> employeeTableView;

    @FXML
    private TableColumn<Employee, String> idColumn;

    @FXML
    private TableColumn<Employee, String> nameColumn;

    @FXML
    private TableColumn<Employee, String> startDateColumn;

    @FXML
    private TableColumn<Employee, String> emailColumn;

    @FXML
    private TableColumn<Employee, String> positionColumn;

    @FXML
    private TableColumn<Employee, String> documentPathsColumn;

    @FXML
    private TableColumn<Employee, String> tasksColumn;

    EmployeeRepository employeeRepository = new EmployeeRepository(Database.getConnection());
    List<Employee> employeeList = employeeRepository.findAll();
    PositionRepository positionRepository = new PositionRepository(Database.getConnection());

    private static final Logger logger = LoggerFactory.getLogger(AdminEmployeeSearchController.class);

    /**
     * Initializes the controller.
     * Sets up table columns and populates the position combo box.
     */
    public void initialize() {
        idColumn.setCellValueFactory(cellValue ->
                new SimpleObjectProperty<>(cellValue.getValue().getId().toString()));

        nameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getEmployeeRecord() != null ?
                        cellData.getValue().getEmployeeRecord().firstName() + " " +
                                cellData.getValue().getEmployeeRecord().lastName() : "N/A"));

        startDateColumn.setCellValueFactory(cellValue ->
                new SimpleObjectProperty<>(cellValue.getValue().getEmployeeRecord().startDate().toString()));

        emailColumn.setCellValueFactory(cellValue ->
                new SimpleObjectProperty<>(cellValue.getValue().getEmail()));

        positionColumn.setCellValueFactory(cellValue ->
                new SimpleObjectProperty<>(cellValue.getValue().getPosition().getName()));

        documentPathsColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDocumentPaths().stream()
                        .sorted(Comparator.reverseOrder())
                        .collect(Collectors.joining("\n"))));

        tasksColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTasks().stream()
                        .sorted((task1, task2) -> task2.getDeadline().compareTo(task1.getDeadline()))
                .map(task -> task.getTaskRecord().name())
                .collect(Collectors.joining("\n"))));

        positionComboBox.setItems(FXCollections.observableArrayList(positionRepository.findAll()
                .stream().map(Position::getName).toList()));
    }

    /**
     * Filters the employees based on the email and position criteria entered by the user.
     * Updates the table view with the filtered results.
     */
    public void filterEmployees() {
        List<Employee> employees = employeeRepository.findAll();

        if (!emailTextField.getText().isEmpty()) {
            employees = employees.stream()
                    .filter(employee -> employee.getEmail().toLowerCase()
                            .contains(emailTextField.getText().toLowerCase()))
                    .toList();
        }
        if (positionComboBox.getValue() != null && !positionComboBox.getValue().isEmpty()) {
            employees = employees.stream()
                    .filter(employee -> employee.getPosition().getName().equals(positionComboBox.getValue()))
                    .toList();
        }

        SortingEmployeesThread sortingThread = new SortingEmployeesThread(employeeTableView, new ArrayList<>(employees));
        Thread thread = new Thread(sortingThread);
        thread.start();
    }

    /**
     * Removes an employee from the system based on the entered ID.
     * Displays confirmation dialog before removal and logs the change.
     * Updates the table view after successful removal.
     */
    public void removeEmployee() {
        if (!idTextField.getText().isEmpty()) {
            Long id;

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

            Optional<Employee> employeeOptional = employeeRepository.findById(id);

            if (employeeOptional.isPresent()) {
                Employee selectedEmployee = employeeOptional.get();

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle(EMPLOYEE_REMOVAL_ALERT_TITLE.getMessage());
                alert.setHeaderText("Confirmation");
                alert.setContentText("Are you sure you want to remove this employee?");

                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    employeeRepository.deleteById(id);

                    ChangeLogEntry entry = new ChangeLogEntry(
                            "Removed employee with id " + id,
                            selectedEmployee.toString(),
                            "N/A",
                            HelloApplication.getLoggedInUser());
                    ChangeLogManager.logChange(entry);

                    LoginManager.removeUser(selectedEmployee.getUsername());

                    employeeList = employeeRepository.findAll();
                    employeeTableView.setItems(FXCollections.observableArrayList(employeeList));
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle(EMPLOYEE_REMOVAL_ALERT_TITLE.getMessage());
                alert.setHeaderText("Error");
                alert.setContentText("Employee with the given ID does not exist!");
                alert.showAndWait();
            }
        }
    }
}
