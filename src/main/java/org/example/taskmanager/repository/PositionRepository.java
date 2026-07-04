package org.example.taskmanager.repository;

import org.example.taskmanager.entity.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

/**
 * Repository class for managing Position entities in the database.
 * Extends AbstractRepository to provide specific CRUD operations for positions.
 */
public class PositionRepository extends AbstractRepository<Position> {
    private static final Logger logger = LoggerFactory.getLogger(PositionRepository.class);

    /**
     * Constructs a new PositionRepository with the given database connection.
     * Initializes the repository to work with the "POSITION" table.
     *
     * @param connection the database connection to be used by this repository
     */
    public PositionRepository(Connection connection) {
        super(connection, "POSITION");
    }

    @Override
    public void save(Position entity) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO POSITION (NAME, DESCRIPTION) VALUES (?, ?)")) {
            stmt.setString(1, entity.getName());
            stmt.setString(2, entity.getDescription());

            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("SQL exception happened",e);
        }
    }

    @Override
    public Position extractEntityFromResultSet(ResultSet rs) throws SQLException {
        return new Position.PositionBuilder()
                .setId(rs.getLong("ID"))
                .setName(rs.getString("NAME"))
                .setDescription(rs.getString("DESCRIPTION"))
                .build();
    }

    @Override
    protected Map<String, String> getColumnMappings() {
        return Map.of(
                "name", "NAME",
                "description", "DESCRIPTION");
    }

    /**
     * Retrieves a Position by its name.
     *
     * @param name the name of the position to retrieve
     * @return an Optional containing the found Position, or empty if not found
     */
    public Optional<Position> getByName(String name) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT ID, NAME, DESCRIPTION FROM POSITION WHERE NAME = ?")) {
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

    /**
     * Checks if a Position has any associated employees.
     *
     * @param positionId the ID of the position to check
     * @return true if the position has associated employees, false otherwise
     */
    public boolean hasAssociatedEmployees(Long positionId) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT COUNT(*) FROM EMPLOYEE WHERE POSITION_ID = ?")) {
            stmt.setLong(1, positionId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            logger.error("Error checking for associated employees", e);
        }
        return false;
    }
}