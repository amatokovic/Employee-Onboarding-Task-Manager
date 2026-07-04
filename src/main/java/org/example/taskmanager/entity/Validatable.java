package org.example.taskmanager.entity;

/**
 * A sealed interface for entities that require validation.
 * This interface can only be implemented by Task and Employee classes.
 */
public sealed interface Validatable permits Task, Employee {
    /**
     * Validates the entity's state.
     * Implementing classes should define their specific validation logic.
     *
     * @throws IllegalStateException if the entity's state is invalid
     */
    void validate();
}
