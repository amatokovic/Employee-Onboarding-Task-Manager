package org.example.taskmanager.entity;

/**
 * Represents a position within the organization.
 * This class uses the Builder pattern for object creation.
 */
public class Position extends Entity {
    private String name;
    private String description;

    /**
     * Private constructor used by the PositionBuilder to create a Position instance.
     * This constructor is not accessible outside the class and should only be called through the builder.
     *
     * @param builder the PositionBuilder containing the position's details
     */
    private Position(PositionBuilder builder) {
        super(builder.id);
        this.name = builder.name;
        this.description = builder.description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Builder class for creating Position instances.
     * Allows for flexible and readable object creation.
     */
    public static class PositionBuilder {
        private Long id;
        private String name;
        private String description;

        /**
         * Sets the ID for the Position.
         *
         * @param id the unique identifier for the Position
         * @return the PositionBuilder instance for method chaining
         */
        public PositionBuilder setId(Long id) {
            this.id = id;
            return this;
        }

        /**
         * Sets the name for the Position.
         *
         * @param name the name of the Position
         * @return the PositionBuilder instance for method chaining
         */
        public PositionBuilder setName(String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets the description for the Position.
         *
         * @param description the description of the Position
         * @return the PositionBuilder instance for method chaining
         */
        public PositionBuilder setDescription(String description) {
            this.description = description;
            return this;
        }

        /**
         * Builds and returns a new Position instance.
         *
         * @return a new Position instance with the set properties
         */
        public Position build() {
            return new Position(this);
        }
    }

    @Override
    public String toString() {
        return "Position " + name + "\ndescription: " + description;
    }
}
