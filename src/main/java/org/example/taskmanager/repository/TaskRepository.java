package org.example.taskmanager.repository;

import org.example.taskmanager.entity.Task;
import org.example.taskmanager.enums.Priority;
import org.example.taskmanager.record.TaskRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.example.taskmanager.enums.Messages.DATE_AND_TIME;
import static org.example.taskmanager.enums.Messages.STATUS;

/**
 * Repository class for managing Task entities in the database.
 * Extends AbstractRepository to provide specific CRUD operations for tasks.
 */
public class TaskRepository extends AbstractRepository<Task> {
    private static final Logger logger = LoggerFactory.getLogger(TaskRepository.class);

    /**
     * Constructs a new TaskRepository with the given database connection.
     * Initializes the repository to work with the "TASK" table.
     *
     * @param connection the database connection to be used by this repository
     */
    public TaskRepository(Connection connection) {
        super(connection, "TASK");
    }

    @Override
    public void save(Task entity) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO TASK(NAME, DESCRIPTION, TYPE, PRIORITY, DATE_AND_TIME, STATUS) VALUES (?, ?, ?, ?, ?, ?)")) {
            stmt.setString(1, entity.getTaskRecord().name());
            stmt.setString(2, entity.getTaskRecord().description());
            stmt.setString(3, entity.getTaskRecord().type());
            stmt.setString(4, entity.getTaskRecord().priority().getName());
            stmt.setTimestamp(5, Timestamp.valueOf(entity.getDeadline()));
            stmt.setString(6, entity.getStatus());

            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("SQL exception happened", e);
        }
    }

    @Override
    public Task extractEntityFromResultSet(ResultSet rs) throws SQLException {
        return new Task.TaskBuilder()
                .setId(rs.getLong("ID"))
                .setTaskRecord(new TaskRecord(
                        rs.getString("NAME"),
                        rs.getString("DESCRIPTION"),
                        rs.getString("TYPE"),
                        Priority.valueOf(rs.getString("PRIORITY"))
                ))
                .setDeadline(rs.getTimestamp(DATE_AND_TIME.getMessage()) != null
                        ? rs.getTimestamp(DATE_AND_TIME.getMessage()).toLocalDateTime()
                        : LocalDateTime.now())
                .setStatus(rs.getString(STATUS.getMessage()) != null ? rs.getString(STATUS.getMessage()) : "Pending")
                .build();
    }

    @Override
    public void deleteById(Long id) {
        try {
            connection.setAutoCommit(false);

            try (PreparedStatement stmt1 = connection.prepareStatement(
                    "DELETE FROM EMPLOYEE_TASK WHERE TASK_ID = ?")) {
                stmt1.setLong(1, id);
                stmt1.executeUpdate();
            }

            try (PreparedStatement stmt = connection.prepareStatement(
                    "DELETE FROM TASK WHERE ID = ?")) {
                stmt.setLong(1, id);
                stmt.executeUpdate();
            }

            connection.commit();
        } catch (SQLException e) {
            logger.error("Failed to delete task with ID: {}", id, e);

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
     * Retrieves a Task by its name.
     *
     * @param name the name of the task to retrieve
     * @return an Optional containing the found Task, or empty if not found
     */
    public Optional<Task> getByName(String name) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT ID, NAME, DESCRIPTION, TYPE, PRIORITY, DATE_AND_TIME, STATUS FROM TASK WHERE NAME = ?")) {
            stmt.setString(1, name);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(extractEntityFromResultSet(rs));
            }
        } catch (SQLException e) {
            logger.error("SQL exception in getByName", e);
        }
        return Optional.empty();
    }

    @Override
    protected Map<String, String> getColumnMappings() {
        return Map.of(
                "name", "NAME",
                "description", "DESCRIPTION",
                "type", "TYPE",
                "priority", "PRIORITY",
                "deadline", "DATE_AND_TIME",
                "status", "STATUS"
        );
    }
}
