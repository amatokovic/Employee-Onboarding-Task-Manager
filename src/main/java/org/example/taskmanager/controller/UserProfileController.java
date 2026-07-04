package org.example.taskmanager.controller;

import javafx.scene.control.Alert;
import org.example.taskmanager.entity.Employee;
import org.example.taskmanager.main.HelloApplication;
import org.example.taskmanager.repository.Database;
import org.example.taskmanager.repository.EmployeeRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller class for displaying user profile information in the task manager application.
 * This utility class provides a static method for showing the current user's profile.
 */
public class UserProfileController {
    static EmployeeRepository employeeRepository = new EmployeeRepository(Database.getConnection());
    static List<Employee> employeeList = employeeRepository.findAll();

    private UserProfileController() { }

    /**
     * Displays the profile information of the currently logged-in user.
     * This method retrieves the user's information from the database and shows it in an alert dialog.
     * If the user is not found, an error alert is displayed.
     */
    public static void showMyProfile(){
        employeeList = employeeRepository.findAll();
        Optional<Employee> currentEmployee = employeeList.stream()
                .filter(employee -> employee.getUsername().equals(HelloApplication.getLoggedInUser()))
                .findFirst();

        if (currentEmployee.isPresent()){
            Employee employee = currentEmployee.get();

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("My Profile");
            alert.setHeaderText("Profile Information:");
            alert.setContentText("Username: " + employee.getUsername() + "\nname: " +
                    employee.getEmployeeRecord().firstName() + " " + employee.getEmployeeRecord().lastName() +
                    "\nstart date: " + employee.getEmployeeRecord().startDate() +
                    "\nemail: " + employee.getEmail() +
                    "\nposition: " + employee.getPosition().getName() +
                    "\ndocuments: " + employee.getDocumentPaths().stream().collect(Collectors.joining("\n")));
            alert.showAndWait();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Employee not found");
            alert.setContentText("Could not find an employee with the logged-in username.");
            alert.showAndWait();
        }
    }
}
