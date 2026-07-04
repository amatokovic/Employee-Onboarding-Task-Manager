package org.example.taskmanager.exception;

/**
 * Exception thrown when a requested position is not found.
 * This exception is typically used in the task manager application
 * to handle cases where a position lookup or operation fails due to
 * the position not existing in the system.
 */
public class PositionNotFoundException extends Exception {
    public PositionNotFoundException(String message) {
        super(message);
    }
}
