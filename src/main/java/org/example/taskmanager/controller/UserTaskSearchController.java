package org.example.taskmanager.controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.taskmanager.entity.Employee;
import org.example.taskmanager.entity.Task;
import org.example.taskmanager.main.HelloApplication;
import org.example.taskmanager.repository.Database;
import org.example.taskmanager.repository.EmployeeRepository;
import org.example.taskmanager.repository.TaskRepository;
import org.example.taskmanager.serialization.ChangeLogEntry;
import org.example.taskmanager.serialization.ChangeLogManager;
import org.example.taskmanager.thread.TaskStatisticsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller class for searching and managing tasks in the user interface of the task manager application.
 * Handles task search, filtering, status updates, and displays task statistics.
 */
public class UserTaskSearchController {
    @FXML
    private TextField nameTextField;
    @FXML
    private DatePicker deadlineDatePicker;
    @FXML
    private ComboBox<String> statusComboBox;
    @FXML
    private TableView<Task> taskTableView;
    @FXML
    private TableColumn<Task, String> idColumn;
    @FXML
    private TableColumn<Task, String> nameColumn;
    @FXML
    private TableColumn<Task, String> descriptionColumn;
    @FXML
    private TableColumn<Task, String> priorityColumn;
    @FXML
    private TableColumn<Task, String> deadlineColumn;
    @FXML
    private TableColumn<Task, String> statusColumn;
    @FXML
    private TableColumn<Task, String> typeColumn;
    @FXML
    private ListView<String> statusAndReminderListView;

    private TaskStatisticsManager statisticsManager;

    private static final String PENDING = "Pending";
    private static final String IN_PROGRESS = "In Progress";
    private static final String COMPLETED = "Completed";

    private static final Logger logger = LoggerFactory.getLogger(UserTaskSearchController.class);

    EmployeeRepository employeeRepository = new EmployeeRepository(Database.getConnection());
    TaskRepository taskRepository = new TaskRepository(Database.getConnection());
    private Employee currentEmployee;

    /**
     * Initializes the controller.
     * Sets up table columns, populates combo boxes, and starts the task statistics manager.
     * Also sets up a double-click listener for updating task status.
     */
    public void initialize() {
        Optional<Employee> optionalEmployee = employeeRepository.findAll().stream()
                .filter(employee -> employee.getUsername().equals(HelloApplication.getLoggedInUser()))
                .findFirst();

        if (optionalEmployee.isPresent()) {
            currentEmployee = optionalEmployee.get();
        } else {
            logger.error("Employee with username {} not found.", HelloApplication.getLoggedInUser());
        }

        idColumn.setCellValueFactory(cellValue ->
                new SimpleObjectProperty<>(cellValue.getValue().getId().toString()));

        nameColumn.setCellValueFactory(cellValue ->
                new SimpleObjectProperty<>(cellValue.getValue().getTaskRecord() != null ?
                        cellValue.getValue().getTaskRecord().name() : "N/A"));

        descriptionColumn.setCellValueFactory(cellValue ->
                new SimpleObjectProperty<>(cellValue.getValue().getTaskRecord() != null ?
                        cellValue.getValue().getTaskRecord().description() : "N/A"));

        priorityColumn.setCellValueFactory(cellValue ->
                new SimpleObjectProperty<>(cellValue.getValue().getTaskRecord() != null ?
                        cellValue.getValue().getTaskRecord().priority().getName() : "N/A"));

        deadlineColumn.setCellValueFactory(cellValue ->
                new SimpleObjectProperty<>(cellValue.getValue().getDeadline().toString()));

        statusColumn.setCellValueFactory(cellValue ->
                new SimpleObjectProperty<>(cellValue.getValue().getStatus()));

        typeColumn.setCellValueFactory(cellValue ->
                new SimpleObjectProperty<>(cellValue.getValue().getTaskRecord() != null ?
                        cellValue.getValue().getTaskRecord().type() : "N/A"));

        statusComboBox.setItems(FXCollections.observableArrayList(PENDING, IN_PROGRESS, COMPLETED));

        taskTableView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Task selectedTask = taskTableView.getSelectionModel().getSelectedItem();
                if (selectedTask != null) {
                    updateTaskStatus(selectedTask);
                }
            }
        });

        statisticsManager = new TaskStatisticsManager(statusAndReminderListView, this::updateStatisticsAndReminders);
        statisticsManager.startPeriodicUpdate();
    }

    /**
     * Updates the task statistics and reminders in the UI.
     * This method is called periodically by the TaskStatisticsManager.
     */
    private void updateStatisticsAndReminders() {
        if (currentEmployee != null) {
            List<Task> tasks = currentEmployee.getTasks();
            statisticsManager.updateStatisticsAndReminders(tasks);
        }
    }

    /**
     * Updates the status of a selected task.
     * This method is called when a user double-clicks on a task in the table.
     *
     * @param selectedTask the task to update
     */
    private void updateTaskStatus(Task selectedTask) {
        String currentStatus = selectedTask.getStatus();
        String newStatus;

        switch (currentStatus) {
            case PENDING:
                newStatus = IN_PROGRESS;
                break;
            case IN_PROGRESS, COMPLETED:
                newStatus = COMPLETED;
                break;
            default:
                return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", newStatus);

        if (taskRepository.update(selectedTask.getId(), updates) > 0) {
            String oldStatus = selectedTask.getStatus();
            selectedTask.setStatus(newStatus);
            taskTableView.refresh();

            ChangeLogEntry entry = new ChangeLogEntry(
                    "Updated status of task with id " + selectedTask.getId(),
                    oldStatus,
                    newStatus,
                    HelloApplication.getLoggedInUser());
            ChangeLogManager.logChange(entry);

        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Update failed");
            alert.setHeaderText("Could not update task status.");
            alert.showAndWait();
        }
    }

    /**
     * Filters the tasks based on the name, deadline, and status criteria entered by the user.
     * Updates the table view with the filtered results.
     */
    public void filterTasks() {
        List<Task> tasks = currentEmployee.getTasks();

        if (!nameTextField.getText().isEmpty()) {
            tasks = tasks.stream()
                    .filter(value -> value.getTaskRecord().name().toLowerCase()
                            .contains(nameTextField.getText().toLowerCase()))
                    .toList();
        }
        if (deadlineDatePicker.getValue() != null) {
            tasks = tasks.stream()
                    .filter(value ->value.getDeadline().isBefore(deadlineDatePicker.getValue().atStartOfDay()))
                    .toList();
        }
        if (statusComboBox.getSelectionModel().getSelectedItem() != null) {
            tasks = tasks.stream()
                    .filter(value -> String.valueOf(value.getStatus()).
                            equals(statusComboBox.getSelectionModel().getSelectedItem()))
                    .toList();
        }
        ObservableList<Task> taskObservableList = FXCollections.observableArrayList(tasks);
        taskTableView.setItems(taskObservableList);
    }

    public void onDestroy() {
        statisticsManager.stopPeriodicUpdate();
    }
}
