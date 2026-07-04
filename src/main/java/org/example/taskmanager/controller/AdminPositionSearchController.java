package org.example.taskmanager.controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.taskmanager.entity.Position;
import org.example.taskmanager.main.HelloApplication;
import org.example.taskmanager.repository.Database;
import org.example.taskmanager.repository.PositionRepository;
import org.example.taskmanager.serialization.ChangeLogEntry;
import org.example.taskmanager.serialization.ChangeLogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

import static org.example.taskmanager.enums.Messages.*;

/**
 * Controller class for searching and managing positions in the admin interface of the task manager application.
 * Handles position search, filtering, and removal operations.
 */
public class AdminPositionSearchController {
    @FXML
    private TextField nameTextField;

    @FXML
    private TextField descriptionTextField;

    @FXML
    private TextField idTextField;

    @FXML
    private TableView<Position> positionTableView;

    @FXML
    private TableColumn<Position, String> idColumn;

    @FXML
    private TableColumn<Position, String> nameColumn;

    @FXML
    private TableColumn<Position, String> descriptionColumn;

    PositionRepository positionRepository = new PositionRepository(Database.getConnection());
    List<Position> positionList = positionRepository.findAll();

    private static final Logger logger = LoggerFactory.getLogger(AdminPositionSearchController.class);

    /**
     * Initializes the controller.
     * Sets up table columns with appropriate cell value factories.
     */
    public void initialize() {
        idColumn.setCellValueFactory(cellValue -> new SimpleObjectProperty<>(cellValue.getValue().getId().toString()));
        nameColumn.setCellValueFactory(cellValue -> new SimpleObjectProperty<>(cellValue.getValue().getName()));
        descriptionColumn.setCellValueFactory(cellValue -> new SimpleObjectProperty<>(cellValue.getValue().getDescription()));
    }

    /**
     * Filters the positions based on the name and description criteria entered by the user.
     * Updates the table view with the filtered results.
     */
    public void filterPositions() {
        List<Position> positions = positionRepository.findAll();

        if (!nameTextField.getText().isEmpty()) {
            positions = positions.stream()
                    .filter(position -> position.getName().toLowerCase()
                            .contains(nameTextField.getText().toLowerCase()))
                    .toList();
        }
        if (!descriptionTextField.getText().isEmpty()) {
            positions = positions.stream()
                    .filter(position -> position.getDescription().toLowerCase()
                            .contains(descriptionTextField.getText().toLowerCase()))
                    .toList();
        }

        ObservableList<Position> positionObservableList = FXCollections.observableArrayList(positions);
        positionTableView.setItems(positionObservableList);
    }

    /**
     * Removes a position from the system based on the entered ID.
     * Checks for associated employees before removal.
     * Displays confirmation dialog before removal and logs the change.
     * Updates the table view after successful removal.
     */
    public void removePosition() {
        if (!idTextField.getText().isEmpty()) {
            try {
                Long id = Long.parseLong(idTextField.getText());

                Optional<Position> positionOptional = positionRepository.findById(id);

                if (positionOptional.isPresent()) {
                    if (positionRepository.hasAssociatedEmployees(id)) {
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("Warning");
                        alert.setHeaderText("Position not deleted");
                        alert.setContentText("This position cannot be deleted because there are employees associated with it.");
                        alert.showAndWait();
                        return;
                    }

                    Position selectedPosition = positionOptional.get();

                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle(TASK_REMOVAL_ALERT_TITLE.getMessage());
                    alert.setHeaderText("Confirmation");
                    alert.setContentText("Are you sure you want to remove this position?");

                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        positionRepository.deleteById(id);

                        ChangeLogEntry entry = new ChangeLogEntry(
                                "Removed position with id " + id,
                                selectedPosition.toString(),
                                "N/A",
                                HelloApplication.getLoggedInUser());
                        ChangeLogManager.logChange(entry);

                        positionList = positionRepository.findAll();
                        positionTableView.setItems(FXCollections.observableArrayList(positionList));
                    }
                } else {
                    showAlert("Task with the given ID does not exist!");
                }
            } catch (NumberFormatException e) {
                logger.error("User entered an incorrect format for ID.", e);
                showAlert("Incorrect format for ID!");
            }
        }
    }

    /**
     * Displays an error alert with the given content.
     *
     * @param content The error message to display in the alert.
     */
    private void showAlert(String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Position removal");
        alert.setHeaderText("Error");
        alert.setContentText(content);
        alert.showAndWait();
    }
}
