package org.example.taskmanager.exception;

/**
 * Exception thrown when invalid credentials are provided during authentication.
 * This exception is typically used in the task manager application to handle
 * cases where a user provides incorrect username or password.
 */
public class InvalidCredentialsException extends Exception {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
