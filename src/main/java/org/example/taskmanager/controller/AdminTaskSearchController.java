package org.example.taskmanager.controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.taskmanager.entity.Task;
import org.example.taskmanager.exception.InvalidIdFormatException;
import org.example.taskmanager.main.HelloApplication;
import org.example.taskmanager.repository.Database;
import org.example.taskmanager.repository.TaskRepository;
import org.example.taskmanager.serialization.ChangeLogEntry;
import org.example.taskmanager.serialization.ChangeLogManager;
import org.example.taskmanager.thread.TaskStatisticsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

import static org.example.taskmanager.enums.Messages.ERROR_ALERT;
import static org.example.taskmanager.enums.Messages.TASK_REMOVAL_ALERT_TITLE;

/**
 * Controller class for searching and managing tasks in the admin interface of the task manager application.
 * Handles task search, filtering, removal operations, and displays task statistics.
 */
public class AdminTaskSearchController {
    @FXML
    private TextField nameTextField;
    @FXML
    private DatePicker deadlineDatePicker;
    @FXML
    private ComboBox<String> statusComboBox;
    @FXML
    private TextField idTextField;
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

    TaskRepository taskRepository = new TaskRepository(Database.getConnection());
    List<Task> taskList = taskRepository.findAll();

    private static final Logger logger = LoggerFactory.getLogger(AdminTaskSearchController.class);

    /**
     * Initializes the controller.
     * Sets up table columns, populates combo boxes, and starts the task statistics manager.
     */
    public void initialize() {
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

        statusComboBox.setItems(FXCollections.observableArrayList("Pending", "In Progress", "Completed"));

        statisticsManager = new TaskStatisticsManager(statusAndReminderListView, this::updateStatisticsAndReminders);
        statisticsManager.startPeriodicUpdate();
    }

    /**
     * Updates the task statistics and reminders in the UI.
     * This method is called periodically by the TaskStatisticsManager.
     */
    private void updateStatisticsAndReminders() {
            statisticsManager.updateStatisticsAndReminders(taskList);
    }

    /**
     * Filters the tasks based on the name, deadline, and status criteria entered by the user.
     * Updates the table view with the filtered results.
     */
    public void filterTasks() {
        List<Task> tasks = taskRepository.findAll();

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
                    .filter(value -> value.getStatus().equals(statusComboBox.getSelectionModel().getSelectedItem()))
                    .toList();
        }

        ObservableList<Task> taskObservableList = FXCollections.observableArrayList(tasks);
        taskTableView.setItems(taskObservableList);
    }

    /**
     * Removes a task from the system based on the entered ID.
     * Displays confirmation dialog before removal and logs the change.
     * Updates the table view after successful removal.
     */
    public void removeTask() {
        Long id;

        if (!idTextField.getText().isEmpty()) {
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

            Optional<Task> taskOptional = taskRepository.findById(id);

            if (taskOptional.isPresent()) {
                Task selectedTask = taskOptional.get();

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle(TASK_REMOVAL_ALERT_TITLE.getMessage());
                alert.setHeaderText("Confirmation");
                alert.setContentText("Are you sure you want to remove this task?");

                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    taskRepository.deleteById(id);

                    ChangeLogEntry entry = new ChangeLogEntry(
                            "Removed task with id " + id,
                            selectedTask.toString(),
                            "N/A",
                            HelloApplication.getLoggedInUser());
                    ChangeLogManager.logChange(entry);

                    taskList = taskRepository.findAll();
                    taskTableView.setItems(FXCollections.observableArrayList(taskList));
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle(TASK_REMOVAL_ALERT_TITLE.getMessage());
                alert.setHeaderText("Error");
                alert.setContentText("Task with the given ID does not exist!");
                alert.showAndWait();
            }
        }
    }
}
