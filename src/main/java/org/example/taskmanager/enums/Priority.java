package org.example.taskmanager.enums;

/**
 * Enumeration of priority levels for tasks in the task manager application.
 * This enum represents different levels of importance or urgency for tasks.
 */
public enum Priority {
    CRITICAL("CRITICAL"),
    HIGH("HIGH"),
    MEDIUM("MEDIUM"),
    LOW("LOW"),
    TRIVIAL("TRIVIAL");

    private final String name;

    Priority(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
