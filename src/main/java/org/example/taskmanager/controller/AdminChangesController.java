package org.example.taskmanager.controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.example.taskmanager.serialization.ChangeLogEntry;
import org.example.taskmanager.serialization.ChangeLogManager;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Controller class for the admin changes view in the task manager application.
 * Manages the display and periodic refresh of the change log entries.
 */
public class AdminChangesController {
    @FXML
    private TableView<ChangeLogEntry> changeLogTable;

    @FXML
    private TableColumn<ChangeLogEntry, String> description;

    @FXML
    private TableColumn<ChangeLogEntry, String> oldValue;

    @FXML
    private TableColumn<ChangeLogEntry, String> newValue;

    @FXML
    private TableColumn<ChangeLogEntry, String> user;

    @FXML
    private TableColumn<ChangeLogEntry, String> timeStamp;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    /**
     * Initializes the controller.
     * Sets up the table columns and starts a periodic refresh of the table data.
     */
    public void initialize() {
        changeLogTable.setFixedCellSize(50);
        description.setCellValueFactory(cellvalue -> new SimpleStringProperty(cellvalue.getValue().getFieldChanged()));
        oldValue.setCellValueFactory(cellvalue -> new SimpleStringProperty(cellvalue.getValue().getOldValue()));
        newValue.setCellValueFactory(cellvalue -> new SimpleStringProperty(cellvalue.getValue().getNewValue()));
        user.setCellValueFactory(cellvalue -> new SimpleStringProperty(cellvalue.getValue().getChangedByRole()));
        timeStamp.setCellValueFactory(cellvalue -> new SimpleStringProperty(cellvalue.getValue().getTimestamp().toString()));

        scheduler.scheduleAtFixedRate(() -> Platform.runLater(this::refreshTable), 0, 3, TimeUnit.SECONDS);
    }

    /**
     * Refreshes the table with the latest change log entries.
     * This method is called periodically to update the displayed data.
     */
    private void refreshTable() {
        List<ChangeLogEntry> changeLogEntries = ChangeLogManager.readChanges();
        changeLogTable.setItems(FXCollections.observableArrayList(changeLogEntries));
    }
}
