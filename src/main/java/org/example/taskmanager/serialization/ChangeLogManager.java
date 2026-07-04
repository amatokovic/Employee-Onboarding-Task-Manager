package org.example.taskmanager.serialization;

import org.example.taskmanager.exception.ChangeLogReadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the logging and retrieval of change log entries.
 * This utility class provides methods to log changes and read the change log.
 */
public class ChangeLogManager {
    private static final String FILE_NAME = "dat/changes_log.bin";
    private static final Object LOCK = new Object();

    private static final Logger logger = LoggerFactory.getLogger(ChangeLogManager.class);

    /**
     * Private constructor to prevent instantiation of this utility class.
     * All methods in this class are static and should be accessed directly through the class.
     */
    private ChangeLogManager() {}

    /**
     * Logs a change by adding a new ChangeLogEntry to the change log file.
     * This method is thread-safe.
     *
     * @param entry the ChangeLogEntry to be logged
     */
    public static void logChange(ChangeLogEntry entry) {
        synchronized (LOCK) {
            List<ChangeLogEntry> changes = readChanges();
            changes.add(entry);
            writeChanges(changes);
        }
    }

    /**
     * Reads and returns all changes from the change log file.
     * This method is thread-safe.
     *
     * @return a List of ChangeLogEntry objects representing all logged changes
     * @throws ChangeLogReadException if an error occurs while reading the change log
     */
    public static List<ChangeLogEntry> readChanges() {
        synchronized (LOCK) {
            File file = new File(FILE_NAME);
            if (!file.exists()) return new ArrayList<>();

            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
                return (List<ChangeLogEntry>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                logger.error("An error occurred while reading change log", e);
            }
            throw new ChangeLogReadException("An error occurred while reading change log");
        }
    }

    /**
     * Writes the given list of changes to the change log file.
     * This method is private and used internally by the class.
     * This method is thread-safe.
     *
     * @param changes the List of ChangeLogEntry objects to be written to the file
     */
    private static void writeChanges(List<ChangeLogEntry> changes) {
        synchronized (LOCK) {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
                oos.writeObject(changes);
            } catch (IOException e) {
                logger.error("An error occurred while writing change log", e);
            }
        }
    }
}