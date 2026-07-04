package org.example.taskmanager.repository;

import javafx.scene.control.Alert;
import org.example.taskmanager.entity.Employee;
import org.example.taskmanager.entity.Position;
import org.example.taskmanager.entity.Task;
import org.example.taskmanager.enums.Priority;
import org.example.taskmanager.record.EmployeeRecord;
import org.example.taskmanager.record.TaskRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.sql.Date;
import java.util.*;

/**
 * Repository class for managing Employee entities in the database.
 * Extends AbstractRepository to provide specific CRUD operations for employees.
 */
public class EmployeeRepository extends AbstractRepository<Employee> {
    private static final Logger logger = LoggerFactory.getLogger(EmployeeRepository.class);

    /**
     * Constructs a new EmployeeRepository with the given database connection.
     *
     * @param connection the database connection to be used by this repository
     */
    public EmployeeRepository(Connection connection) {
        super(connection, "EMPLOYEE");
    }

    @Override
    public void save(Employee entity) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO EMPLOYEE (USERNAME, PASSWORD, FIRST_NAME, LAST_NAME, START_DATE, EMAIL, POSITION_ID) VALUES (?, ?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, entity.getUsername());
            stmt.setString(2, entity.getPassword());
            stmt.setString(3, entity.getEmployeeRecord().firstName());
            stmt.setString(4, entity.getEmployeeRecord().lastName());
            stmt.setDate(5, Date.valueOf(entity.getEmployeeRecord().startDate()));
            stmt.setString(6, entity.getEmail());
            stmt.setLong(7, entity.getPosition().getId());

            stmt.executeUpdate();

            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                entity.setId(generatedKeys.getLong(1));
            } else {
                throw new SQLException("Creating employee failed, no ID obtained.");
            }

            try (PreparedStatement taskIdStmt = connection.prepareStatement(
                    "INSERT INTO EMPLOYEE_TASK(EMPLOYEE_ID, TASK_ID) VALUES(?, ?);")) {
                for (Task task : entity.getTasks()) {
                    taskIdStmt.setLong(1, entity.getId());
                    taskIdStmt.setLong(2, task.getId());

                    taskIdStmt.addBatch();
                }
                taskIdStmt.executeBatch();
            }

        } catch (SQLException e) {
            logger.error("Failed to insert employee", e);
        }
    }

    @Override
    public Employee extractEntityFromResultSet(ResultSet rs) throws SQLException {
        long employeeId = rs.getLong("ID");
        long positionId = rs.getLong("POSITION_ID");

        PositionRepository positionRepository = new PositionRepository(connection);
        Position position = positionRepository.findById(positionId)
                .orElseThrow(() -> new SQLException("Position not found"));

        Set<String> documents = getDocumentsForEmployee(employeeId);

        List<Task> tasks = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT TASK.* FROM TASK JOIN EMPLOYEE_TASK ON TASK.ID = EMPLOYEE_TASK.TASK_ID WHERE EMPLOYEE_TASK.EMPLOYEE_ID = ?")) {
            stmt.setLong(1, employeeId);

            ResultSet taskResultSet = stmt.executeQuery();
            while (taskResultSet.next()) {
                tasks.add(new Task.TaskBuilder()
                        .setId(taskResultSet.getLong("ID"))
                        .setTaskRecord(new TaskRecord(
                                taskResultSet.getString("NAME"),
                                taskResultSet.getString("DESCRIPTION"),
                                taskResultSet.getString("TYPE"),
                                Priority.valueOf(taskResultSet.getString("PRIORITY"))
                        ))
                        .setDeadline(taskResultSet.getTimestamp("DATE_AND_TIME").toLocalDateTime())
                        .setStatus(taskResultSet.getString("STATUS"))
                        .build());
            }
        }

        return new Employee.EmployeeBuilder()
                .setId(rs.getLong("ID"))
                .setUsername(rs.getString("USERNAME"))
                .setPassword(rs.getString("PASSWORD"))
                .setEmployeeRecord(new EmployeeRecord(
                        rs.getString("FIRST_NAME"),
                        rs.getString("LAST_NAME"),
                        rs.getDate("START_DATE").toLocalDate()
                ))
                .setEmail(rs.getString("EMAIL"))
                .setPosition(position)
                .setTasks(tasks)
                .setDocumentPaths(documents)
                .build();
    }

    /**
     * Retrieves all document paths for the specified employee.
     *
     * @param employeeId the ID of the employee
     * @return a Set of document paths
     */
    public Set<String> getDocumentsForEmployee(Long employeeId) {
        Set<String> documents = new HashSet<>();

        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT DOCUMENT_PATH FROM DOCUMENT WHERE EMPLOYEE_ID = ?")) {
            stmt.setLong(1, employeeId);

            try (ResultSet resultSet = stmt.executeQuery()) {
                while (resultSet.next()) {
                    documents.add(resultSet.getString("DOCUMENT_PATH"));
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to fetch documents for employee", e);
        }
        return documents;
    }

    @Override
    public void deleteById(Long id) {
        try {
            connection.setAutoCommit(false);

            try (PreparedStatement stmt1 = connection.prepareStatement(
                    "DELETE FROM EMPLOYEE_TASK WHERE EMPLOYEE_ID = ?")) {
                stmt1.setLong(1, id);
                stmt1.executeUpdate();
            }

            try (PreparedStatement stmt2 = connection.prepareStatement("DELETE FROM DOCUMENT WHERE EMPLOYEE_ID = ?")) {
                stmt2.setLong(1, id);
                stmt2.executeUpdate();
            }

            try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM EMPLOYEE WHERE ID = ?")) {
                stmt.setLong(1, id);
                stmt.executeUpdate();
            }

            connection.commit();
        } catch (SQLException e) {
            logger.error("Failed to delete employee with ID: {}", id, e);

            try {
                connection.rollback();
            } catch (SQLException e1) {
                logger.error("Failed to rollback transaction", e1);
            }
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                logger.error("Failed to reset auto-commit", e);
            }
        }
    }

    /**
     * Adds a document path to the specified employee.
     *
     * @param employeeId   the ID of the employee
     * @param documentPath the path to the document
     */
    public void addDocumentToEmployee(Long employeeId, String documentPath) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO DOCUMENT (EMPLOYEE_ID, DOCUMENT_PATH) VALUES (?, ?)")) {
            stmt.setLong(1, employeeId);
            stmt.setString(2, documentPath);

            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Failed to add document to employee", e);

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Error");
            alert.setHeaderText("Failed to save document.");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Adds a task to the specified employee.
     *
     * @param employeeId the ID of the employee
     * @param taskId     the ID of the task
     */
    public void addTaskToEmployee(Long employeeId, Long taskId) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO EMPLOYEE_TASK (EMPLOYEE_ID, TASK_ID) VALUES (?, ?)")) {
            stmt.setLong(1, employeeId);
            stmt.setLong(2, taskId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Failed to add task to employee", e);
        }
    }

    @Override
    protected Map<String, String> getColumnMappings() {
        return Map.of(
                "username", "USERNAME",
                "password", "PASSWORD",
                "position", "POSITION_ID",
                "task", "TASK_ID"
        );
    }
}
