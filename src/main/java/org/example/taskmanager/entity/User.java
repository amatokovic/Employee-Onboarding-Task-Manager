package org.example.taskmanager.entity;

/**
 * An abstract base class representing a user in the task manager system.
 * Extends the Entity class and provides basic user attributes and functionality.
 */
public abstract class User extends Entity {
    protected String username;
    protected String password;

    /**
     * Constructs a new User with the specified ID, username, and password.
     * This constructor is protected and can only be called by subclasses.
     *
     * @param id       the unique identifier for the user
     * @param username the username of the user
     * @param password the password of the user
     */
    protected User(Long id, String username, String password) {
        super(id);
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
