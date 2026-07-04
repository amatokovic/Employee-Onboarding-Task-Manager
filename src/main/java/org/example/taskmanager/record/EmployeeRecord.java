package org.example.taskmanager.record;

import java.time.LocalDate;

/**
 * Represents an immutable record of employee information.
 * Contains basic details about an employee such as name and start date.
 *
 * @param firstName the first name of the employee
 * @param lastName the last name of the employee
 * @param startDate the date when the employee started working
 */
public record EmployeeRecord(String firstName, String lastName, LocalDate startDate) {
}
