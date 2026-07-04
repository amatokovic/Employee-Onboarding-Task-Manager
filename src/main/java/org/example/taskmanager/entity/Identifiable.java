package org.example.taskmanager.entity;

/**
 * Represents an entity with a unique identifier.
 * Implementing classes should provide a method to retrieve the identifier.
 */
public interface Identifiable {
    /**
     * Retrieves the unique identifier of the entity.
     * @return the unique identifier as a Long
     */
    Long getId();
}
