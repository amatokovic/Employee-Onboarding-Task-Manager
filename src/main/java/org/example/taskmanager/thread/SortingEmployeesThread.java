package org.example.taskmanager.thread;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import org.example.taskmanager.entity.Employee;

import java.util.Comparator;
import java.util.List;

/**
 * A thread class for sorting employees in a TableView.
 * This class implements Runnable to allow for background sorting of employees.
 */
public class SortingEmployeesThread implements Runnable {
    private final TableView<Employee> employeeTableView;
    private final List<Employee> employeeList;

    /**
     * Constructs a new SortingEmployeesThread.
     *
     * @param employeeTableView the TableView to be updated with sorted employees
     * @param employeeList the list of employees to be sorted
     */
    public SortingEmployeesThread(TableView<Employee> employeeTableView, List<Employee> employeeList) {
        this.employeeTableView = employeeTableView;
        this.employeeList = employeeList;
    }

    /**
     * Sorts the employee list by start date and updates the TableView.
     * This method is executed when the thread is run.
     */
    @Override
    public void run() {
        employeeList.sort(Comparator.comparing(employee -> employee.getEmployeeRecord().startDate()));

        ObservableList<Employee> sortedEmployeeObservableList = FXCollections.observableList(employeeList);
        javafx.application.Platform.runLater(() -> employeeTableView.setItems(sortedEmployeeObservableList));
    }
}
