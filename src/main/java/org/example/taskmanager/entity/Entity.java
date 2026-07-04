package org.example.taskmanager.entity;

/**
 * An abstract base class for entities in the task manager system.
 * Implements the Identifiable interface and provides basic id functionality.
 */
public abstract class Entity implements Identifiable {
    private Long id;

    /**
     * Constructs a new Entity with the specified ID.
     *
     * @param id the unique identifier for the entity
     */
    protected Entity(Long id) {
        this.id = id;
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
