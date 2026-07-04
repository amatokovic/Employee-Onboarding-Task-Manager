package org.example.taskmanager.enums;

/**
 * Enumeration of predefined messages used throughout the task manager application.
 * This enum provides a centralized location for managing common messages and labels.
 */
public enum Messages {
    TASK_REMOVAL_ALERT_TITLE("Task removal"),
    EMPLOYEE_REMOVAL_ALERT_TITLE("Employee removal"),
    READING_CREDENTIALS_FILE_EXCEPTION("Error while reading credentials file"),
    ERROR_ALERT("ERROR"),
    SQL_EXCEPTION_MESSAGE("SQL exception happened"),
    DATE_AND_TIME("DATE_AND_TIME"),
    STATUS("STATUS");

    private final String message;

    Messages(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
