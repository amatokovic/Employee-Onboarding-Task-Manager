package org.example.taskmanager.entity;

import org.example.taskmanager.enums.Priority;
import org.example.taskmanager.exception.DateOutOfRangeException;
import org.example.taskmanager.record.TaskRecord;

import java.time.LocalDateTime;

/**
 * Represents a task in the task management system.
 * This class extends Entity and implements Validatable interface.
 * It uses the Builder pattern for object creation.
 */
public final class Task extends Entity implements Validatable {
    private final TaskRecord taskRecord;
    private final LocalDateTime deadline;
    private String status;

    /**
     * Private constructor used by the TaskBuilder to create a Task instance.
     * This constructor is not accessible outside the class and should only be called through the builder.
     *
     * @param builder the TaskBuilder containing the task's details
     */
    private Task(TaskBuilder builder) {
        super(builder.id);
        this.taskRecord = builder.taskRecord;
        this.deadline = builder.deadline;
        this.status = builder.status != null ? builder.status : "Pending";
    }

    public TaskRecord getTaskRecord() {
        if (taskRecord == null) {
            return new TaskRecord("N/A", "N/A", "N/A", Priority.LOW);
        }
        return taskRecord;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Builder class for creating Task instances.
     * Allows for flexible and readable object creation.
     */
    public static class TaskBuilder {
        private Long id;
        private TaskRecord taskRecord;
        private LocalDateTime deadline;
        private String status;

        /**
         * Sets the ID for the Task.
         *
         * @param id the unique identifier for the Task
         * @return the TaskBuilder instance for method chaining
         */
        public TaskBuilder setId(Long id) {
            this.id = id;
            return this;
        }

        /**
         * Sets the TaskRecord for the Task.
         *
         * @param taskRecord the TaskRecord containing task details
         * @return the TaskBuilder instance for method chaining
         */
        public TaskBuilder setTaskRecord(TaskRecord taskRecord) {
            this.taskRecord = taskRecord;
            return this;
        }

        /**
         * Sets the deadline for the Task.
         *
         * @param deadline the deadline of the Task
         * @return the TaskBuilder instance for method chaining
         */
        public TaskBuilder setDeadline(LocalDateTime deadline) {
            this.deadline = deadline;
            return this;
        }

        /**
         * Sets the status for the Task.
         *
         * @param status the status of the Task
         * @return the TaskBuilder instance for method chaining
         */
        public TaskBuilder setStatus(String status) {
            this.status = status;
            return this;
        }

        /**
         * Builds and returns a new Task instance.
         *
         * @return a new Task instance with the set properties
         */
        public Task build() {
            return new Task(this);
        }
    }

    @Override
    public void validate() {
        if (deadline.isBefore(LocalDateTime.now())) {
            throw new DateOutOfRangeException("Deadline cannot be in the past!");
        }
    }

    @Override
    public String toString() {
        return "Task " + getTaskRecord().name() +
                "\ndescription: " + getTaskRecord().description() +
                "\ntype: " + getTaskRecord().type() +
                "\npriority: " + getTaskRecord().priority().getName() +
                "\ndeadline: " + deadline.toString() +
                "\nstatus: " + status;
    }
}
