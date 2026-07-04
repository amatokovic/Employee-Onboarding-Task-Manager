package org.example.taskmanager.repository;

import org.example.taskmanager.entity.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.example.taskmanager.enums.Messages.SQL_EXCEPTION_MESSAGE;

/**
 * An abstract base class for repository implementations.
 * Provides common CRUD operations for entities extending the Entity class.
 *
 * @param <T> the type of entity this repository manages, must extend Entity
 */
public abstract class AbstractRepository<T extends Entity> {
    protected Connection connection;
    protected final String tableName;

    private static final String WHERE_ID_CONDITION = " WHERE ID = ?";
    private static final Logger logger = LoggerFactory.getLogger(AbstractRepository.class);

    /**
     * Constructs a new AbstractRepository with the given database connection and table name.
     * This constructor is protected and can only be called by subclasses.
     *
     * @param connection the database connection to be used by this repository
     * @param tableName  the name of the database table this repository will operate on
     */
    protected AbstractRepository(Connection connection, String tableName) {
        this.connection = connection;
        this.tableName = tableName;
    }

    /**
     * Saves the given entity to the database.
     *
     * @param entity the entity to save
     */
    public abstract void save(T entity);

    /**
     * Finds an entity by its ID.
     *
     * @param id the ID of the entity to find
     * @return an Optional containing the found entity, or empty if not found
     */
    public Optional<T> findById(Long id) {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM " + tableName + WHERE_ID_CONDITION)) {
            stmt.setLong(1, id);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(extractEntityFromResultSet(rs));
            }
        } catch (SQLException e) {
            logger.error(SQL_EXCEPTION_MESSAGE.getMessage(), e);
        }
        return Optional.empty();
    }

    /**
     * Maps a ResultSet to an entity object.
     *
     * @param rs the ResultSet to map
     * @return the mapped entity
     * @throws SQLException if a database access error occurs
     */
    public abstract T extractEntityFromResultSet(ResultSet rs) throws SQLException;

    /**
     * Retrieves all entities from the database.
     *
     * @return a List of all entities
     */
    public List<T> findAll() {
        List<T> entities = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM " + tableName);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                entities.add(extractEntityFromResultSet(rs));
            }
        } catch (SQLException e) {
            logger.error(SQL_EXCEPTION_MESSAGE.getMessage(), e);
        }

        return entities;
    }

    /**
     * Deletes an entity by its ID.
     *
     * @param id the ID of the entity to delete
     */
    public void deleteById(Long id) {
        try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM " + tableName + WHERE_ID_CONDITION)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch(SQLException e) {
            logger.error(SQL_EXCEPTION_MESSAGE.getMessage(), e);
        }
    }

    /**
     * Returns a mapping of entity field names to database column names.
     *
     * @return a Map of field names to column names
     */
    protected abstract Map<String, String> getColumnMappings();

    /**
     * Updates an entity with the given ID using the provided updates.
     *
     * @param id      the ID of the entity to update
     * @param updates a Map of field names to new values
     * @return the number of rows affected by the update
     */
    public int update(Long id, Map<String, Object> updates) {
        if (updates.isEmpty()) {
            return 0;
        }

        Map<String, String> columnMappings = getColumnMappings();

        StringBuilder query = new StringBuilder("UPDATE ").append(tableName).append(" SET ");
        List<Object> values = new ArrayList<>();

        for (Map.Entry<String, Object> entry : updates.entrySet()) {
            String columnName = columnMappings.getOrDefault(entry.getKey(), entry.getKey());
            query.append(columnName).append(" = ?, ");
            values.add(entry.getValue());
        }

        query.delete(query.length() - 2, query.length());
        query.append(WHERE_ID_CONDITION);

        try (PreparedStatement stmt = connection.prepareStatement(query.toString())) {
            int index = 1;
            for (Object value : values) {
                stmt.setObject(index++, value);
            }
            stmt.setLong(index, id);

            return stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("SQL exception happened", e);
            return 0;
        }
    }
}
