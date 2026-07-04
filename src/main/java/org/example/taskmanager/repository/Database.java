package org.example.taskmanager.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Utility class for managing database connections.
 * This class provides methods to establish and close database connections.
 */
public class Database {
    private static final Logger logger = LoggerFactory.getLogger(Database.class);

    /**
     * Establishes and returns a database connection using properties from a configuration file.
     *
     * @return a Connection object if successful, null if an error occurs
     */
    public static Connection getConnection() {
        try (FileInputStream fis = new FileInputStream("database.properties")) {
            Properties props = new Properties();
            props.load(fis);

            return DriverManager.getConnection(
                    props.getProperty("databaseUrl"),
                    props.getProperty("username"),
                    props.getProperty("password"));
        } catch (IOException | SQLException e) {
            logger.error("SQL exception happened", e);
            return null;
        }
    }

    /**
     * Closes the given database connection.
     *
     * @param connection the Connection object to close
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                logger.error("SQL exception happened", e);
            }
        }
    }

    /**
     * Private constructor to prevent instantiation of this utility class.
     * All methods in this class are static and should be accessed directly through the class.
     */
    private Database(){}
}
