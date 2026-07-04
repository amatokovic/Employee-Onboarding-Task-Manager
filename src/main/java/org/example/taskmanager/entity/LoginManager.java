package org.example.taskmanager.entity;

import org.example.taskmanager.exception.InvalidCredentialsException;
import org.example.taskmanager.main.HelloApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import static org.example.taskmanager.enums.Messages.READING_CREDENTIALS_FILE_EXCEPTION;

/**
 * Manages user authentication and credential operations.
 * This class provides methods for user authentication, password hashing, and user management.
 */
public class LoginManager {
    private static final String ADMIN_FILE = "dat/admin.txt";
    private static final String USER_FILE = "dat/user.txt";

    private static final Logger logger = LoggerFactory.getLogger(LoginManager.class);

    /**
     * Private constructor to prevent instantiation of this utility class.
     * All methods in this class are static and should be accessed directly through the class.
     */
    private LoginManager() { }

    /**
     * Hashes the given password using SHA-256 algorithm.
     *
     * @param password the password to hash
     * @return the hashed password as a hexadecimal string, or null if hashing fails
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());

            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            logger.error("Error while hashing password", e);
        }
        return null;
    }

    /**
     * Authenticates a user with the given username and password.
     *
     * @param username the username to authenticate
     * @param password the password to authenticate
     * @return true if authentication is successful, false otherwise
     * @throws InvalidCredentialsException if the credentials are invalid
     */
    public static Boolean authenticate(String username, String password) throws InvalidCredentialsException {
        String fileName;

        if (username.equals("admin")) {
            fileName = ADMIN_FILE;
        } else {
            fileName = USER_FILE;
        }

        String hashedPassword = hashPassword(password);

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");

                if (parts.length == 2 && parts[0].equals(username) && parts[1].equals(hashedPassword)) {
                    HelloApplication.setLoggedInUser(parts[0]);
                    return true;
                }
            }
        } catch (IOException e) {
            logger.error(READING_CREDENTIALS_FILE_EXCEPTION.getMessage(), e);
        }
        throw new InvalidCredentialsException("Invalid credentials");
    }

    /**
     * Adds a new user to the user file.
     *
     * @param namePass a Pair containing the username and password of the new user
     */
    public static void addUser(Pair<String, String> namePass) {
        try (FileWriter writer = new FileWriter(USER_FILE, true)) {
            writer.write(namePass.getKey() + ":" + hashPassword(namePass.getValue()) + "\n");
        } catch (IOException e) {
            logger.error(READING_CREDENTIALS_FILE_EXCEPTION.getMessage(), e);
        }
    }

    /**
     * Removes a user from the user file.
     *
     * @param username the username of the user to remove
     */
    public static void removeUser(String username) {
        modifyUserFile(username, null, null, true);
    }

    /**
     * Updates a user's credentials in the user file.
     *
     * @param username    the current username of the user
     * @param newUsername the new username (if changing)
     * @param newPassword the new password (if changing)
     */
    public static void updateUser(String username, String newUsername, String newPassword) {
        modifyUserFile(username, newUsername, newPassword, false);
    }

    /**
     * Modifies the user file based on the specified operation.
     *
     * @param username    the username of the user to modify
     * @param newUsername the new username (if updating)
     * @param newPassword the new password (if updating)
     * @param isRemove    true if removing the user, false if updating
     */
    public static void modifyUserFile(String username, String newUsername, String newPassword, boolean isRemove) {
        List<String> users = readUsersFromFile(username, newUsername, newPassword, isRemove);
        writeUsersToFile(users);
    }

    /**
     * Reads users from the user file and processes them based on the specified operation.
     *
     * @param username    the username to process
     * @param newUsername the new username (if updating)
     * @param newPassword the new password (if updating)
     * @param isRemove    true if removing the user, false if updating
     * @return a List of processed user entries
     */
    private static List<String> readUsersFromFile(String username, String newUsername, String newPassword, boolean isRemove) {
        List<String> users = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                users.add(processUserLine(line, username, newUsername, newPassword, isRemove));
            }
            users.removeIf(String::isEmpty);
        } catch (IOException e) {
            logger.error("Error while reading credentials file", e);
        }
        return users;
    }

    /**
     * Processes a single user line from the user file.
     *
     * @param line        the line to process
     * @param username    the username to match
     * @param newUsername the new username (if updating)
     * @param newPassword the new password (if updating)
     * @param isRemove    true if removing the user, false if updating
     * @return the processed line, or an empty string if the user should be removed
     */
    private static String processUserLine(String line, String username, String newUsername, String newPassword, boolean isRemove) {
        String[] parts = line.split(":");

        if (parts.length == 2 && parts[0].equals(username)) {
            return isRemove ? "" : formatUpdatedUser(username, newUsername, newPassword, parts[1]);
        }
        return line;
    }

    /**
     * Formats an updated user entry.
     *
     * @param oldUsername     the current username
     * @param newUsername     the new username (if changing)
     * @param newPassword     the new password (if changing)
     * @param currentPassword the current hashed password
     * @return a formatted string representing the updated user entry
     */
    private static String formatUpdatedUser(String oldUsername, String newUsername, String newPassword, String currentPassword) {
        String updatedUsername = (newUsername != null && !newUsername.isEmpty()) ? newUsername : oldUsername;
        String updatedPassword = (newPassword != null && !newPassword.isEmpty()) ? hashPassword(newPassword) : currentPassword;
        return updatedUsername + ":" + updatedPassword;
    }

    /**
     * Writes the list of users to the user file.
     *
     * @param users the List of user entries to write
     */
    private static void writeUsersToFile(List<String> users) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USER_FILE))) {
            for (String user : users) {
                writer.write(user);
                writer.newLine();
            }
        } catch (IOException e) {
            logger.error("Error while writing in credentials file", e);
        }
    }
}