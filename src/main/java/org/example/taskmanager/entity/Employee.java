package org.example.taskmanager.entity;

import org.example.taskmanager.exception.DateOutOfRangeException;
import org.example.taskmanager.record.EmployeeRecord;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents an employee in the task management system.
 * This class extends User and implements Validatable interface.
 * It uses the Builder pattern for object creation.
 */
public final class Employee extends User implements Validatable {
    private final EmployeeRecord employeeRecord;
    private final String email;
    private Position position;
    private final Set<String> documentPaths;
    private final List<Task> tasks;

    /**
     * Private constructor used by the Builder.
     *
     * @param builder the EmployeeBuilder instance
     */
    private Employee(EmployeeBuilder builder) {
        super(builder.id, builder.username, builder.password);
        this.employeeRecord = builder.employeeRecord;
        this.email = builder.email;
        this.position = builder.position;
        this.documentPaths = builder.documentPaths;
        this.tasks = builder.tasks;
    }

    public EmployeeRecord getEmployeeRecord() { return employeeRecord; }

    public String getEmail() { return email; }

    public Position getPosition() { return position; }

    public void setPosition(Position position) { this.position = position; }

    public Set<String> getDocumentPaths() { return documentPaths; }

    public List<Task> getTasks() { return tasks; }

    /**
     * Builder class for creating Employee instances.
     * Allows for flexible and readable object creation.
     */
    public static class EmployeeBuilder {
        private Long id;
        private String username;
        private String password;
        private EmployeeRecord employeeRecord;
        private String email;
        private Position position;
        private Set<String> documentPaths = new HashSet<>();
        private List<Task> tasks = new ArrayList<>();

        /**
         * Sets the ID for the Employee.
         *
         * @param id the unique identifier for the Employee
         * @return the EmployeeBuilder instance for method chaining
         */
        public EmployeeBuilder setId(Long id) {
            this.id = id;
            return this;
        }

        /**
         * Sets the username for the Employee.
         *
         * @param username the username of the Employee
         * @return the EmployeeBuilder instance for method chaining
         */
        public EmployeeBuilder setUsername(String username) {
            this.username = username;
            return this;
        }


        /**
         * Sets the password for the Employee.
         *
         * @param password the password of the Employee
         * @return the EmployeeBuilder instance for method chaining
         */
        public EmployeeBuilder setPassword(String password) {
            this.password = password;
            return this;
        }

        /**
         * Sets the EmployeeRecord for the Employee.
         *
         * @param employeeRecord the EmployeeRecord containing employee details
         * @return the EmployeeBuilder instance for method chaining
         */
        public EmployeeBuilder setEmployeeRecord(EmployeeRecord employeeRecord) {
            this.employeeRecord = employeeRecord;
            return this;
        }

        /**
         * Sets the email for the Employee.
         *
         * @param email the email of the Employee
         * @return the EmployeeBuilder instance for method chaining
         */
        public EmployeeBuilder setEmail(String email) {
            this.email = email;
            return this;
        }

        /**
         * Sets the Position for the Employee.
         *
         * @param position the Position of the Employee
         * @return the EmployeeBuilder instance for method chaining
         */
        public EmployeeBuilder setPosition(Position position) {
            this.position = position;
            return this;
        }

        /**
         * Sets the document paths for the Employee.
         *
         * @param documentPaths a Set of document paths associated with the Employee
         * @return the EmployeeBuilder instance for method chaining
         */
        public EmployeeBuilder setDocumentPaths(Set<String> documentPaths) {
            this.documentPaths = documentPaths;
            return this;
        }

        /**
         * Sets the tasks for the Employee.
         *
         * @param tasks a List of Task objects assigned to the Employee
         * @return the EmployeeBuilder instance for method chaining
         */
        public EmployeeBuilder setTasks(List<Task> tasks) {
            this.tasks = tasks;
            return this;
        }

        /**
         * Builds and returns a new Employee instance.
         *
         * @return a new Employee instance with the set properties
         */
        public Employee build() {
            return new Employee(this);
        }
    }

    /**
     * Validates the given email address.
     *
     * @param email the email address to validate
     * @return true if the email is valid, false otherwise
     */
    public static boolean validateEmail(String email) {
        Pattern emailRegex = Pattern.compile(
                "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = emailRegex.matcher(email);

        return matcher.matches();
    }

    @Override
    public void validate() {
        if (getEmployeeRecord().startDate().isAfter(LocalDate.now())) {
            throw new DateOutOfRangeException("Start date cannot be in the future!");
        }
    }

    @Override
    public String toString() {
        return "Employee " + employeeRecord.firstName() + " " + employeeRecord.lastName() +
                "\nemail: " + email +
                "\nposition: " + position.getName();
    }
}
