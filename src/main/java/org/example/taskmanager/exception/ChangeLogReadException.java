package org.example.taskmanager.exception;

/**
 * Exception thrown when there is an error reading the change log.
 * This exception is typically used in the context of changelog operations
 * in the task manager application.
 */
public class ChangeLogReadException extends RuntimeException {
    public ChangeLogReadException(String message) {
        super(message);
    }
}
