package org.example.taskmanager.record;

import org.example.taskmanager.enums.Priority;

/**
 * Represents an immutable record of task information.
 * Contains essential details about a task including its name, description, type, and priority.
 *
 * @param name the name of the task
 * @param description a brief description of the task
 * @param type the type or category of the task
 * @param priority the priority level of the task
 */
public record TaskRecord(String name, String description, String type, Priority priority) {
}
