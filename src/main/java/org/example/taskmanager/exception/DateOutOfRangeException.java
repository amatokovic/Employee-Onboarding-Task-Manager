package org.example.taskmanager.exception;

/**
 * Exception thrown when a date is out of the expected or allowed range.
 * This exception is typically used in the task manager application to handle
 * cases where dates (such as deadlines or start dates) are set to invalid values.
 */
public class DateOutOfRangeException extends RuntimeException {
    public DateOutOfRangeException(String message) {
        super(message);
    }
}
