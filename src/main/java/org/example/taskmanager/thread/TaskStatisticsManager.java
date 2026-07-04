package org.example.taskmanager.thread;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import org.example.taskmanager.entity.Task;
import org.example.taskmanager.main.HelloApplication;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Manages task statistics and reminders for the task manager application.
 * This class handles periodic updates of task statistics and displays them in a ListView.
 */
public class TaskStatisticsManager {
    private static final String PENDING = "Pending";
    private static final String IN_PROGRESS = "In Progress";
    private static final String COMPLETED = "Completed";

    private final ListView<String> statusAndReminderListView;
    private final Runnable updateCallback;
    private ScheduledExecutorService scheduler;
    private double lastCompletedPercentage = 0.0;
    private boolean isInitialLoad = true;

    /**
     * Constructs a new TaskStatisticsManager.
     *
     * @param taskStatusListView the ListView to display task statistics and reminders
     * @param updateCallback a Runnable to be called for each update, typically used to refresh task data
     */
    public TaskStatisticsManager(ListView<String> taskStatusListView, Runnable updateCallback) {
        this.statusAndReminderListView = taskStatusListView;
        this.updateCallback = updateCallback;
    }

    /**
     * Starts periodic updates of task statistics.
     * Updates occur every 5 seconds.
     */
    public void startPeriodicUpdate() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::updateTaskStatistics, 0, 5, TimeUnit.SECONDS);
    }

    /**
     * Triggers an update of task statistics.
     * This method is called periodically by the scheduler.
     */
    public void updateTaskStatistics() {
        updateCallback.run();
    }

    /**
     * Updates the statistics and reminders based on the provided list of tasks.
     * Calculates percentages of tasks in different statuses and creates reminders for upcoming tasks.
     *
     * @param tasks the list of tasks to analyze
     */
    public void updateStatisticsAndReminders(List<Task> tasks) {
        long totalTasks = tasks.size();
        long pendingTasks = tasks.stream().filter(task -> PENDING.equals(task.getStatus())).count();
        long inProgressTasks = tasks.stream().filter(task -> IN_PROGRESS.equals(task.getStatus())).count();
        long completedTasks = tasks.stream().filter(task -> COMPLETED.equals(task.getStatus())).count();

        double pendingPercentage = totalTasks > 0 ? (double) pendingTasks / totalTasks * 100 : 0.0;
        double inProgressPercentage = totalTasks > 0 ? (double) inProgressTasks / totalTasks * 100 : 0.0;
        double completedPercentage = totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0.0;

        List<String> items = new ArrayList<>();
        items.add("------ Progress ------");
        items.add(String.format("Pending: %.2f%%", pendingPercentage));
        items.add(String.format("In Progress: %.2f%%", inProgressPercentage));
        items.add(String.format("Completed: %.2f%%", completedPercentage));
        items.add("\n");
        items.add("------ Reminders ------");

        List<Task> reminderTasks = tasks.stream()
                .filter(task -> !task.getStatus().equals(COMPLETED))
                .sorted(Comparator.comparing(Task::getDeadline))
                .toList();

        for (Task task : reminderTasks) {
            String reminderText = task.getTaskRecord().name() + " - Due: " + task.getDeadline();
            if (task.getDeadline().isBefore(LocalDateTime.now().plusDays(1))) {
                reminderText = "URGENT: " + reminderText;
            }
            items.add(reminderText);
        }

        Platform.runLater(() -> {
            statusAndReminderListView.setItems(FXCollections.observableArrayList(items));

            if (!isInitialLoad && completedPercentage > lastCompletedPercentage && !HelloApplication.getLoggedInUser().equals("admin")) {
                showCongratulationsPopup(completedPercentage);
            }

            lastCompletedPercentage = completedPercentage;
            isInitialLoad = false;
        });
    }

    /**
     * Displays a congratulations popup when the percentage of completed tasks increases.
     *
     * @param completedPercentage the new percentage of completed tasks
     */
    private void showCongratulationsPopup(double completedPercentage) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Congratulations");
        alert.setHeaderText("Good job!");
        alert.setContentText(String.format("Percentage of completed tasks is now %.2f%%!", completedPercentage));
        alert.show();
    }

    /**
     * Stops the periodic update of task statistics.
     * This method should be called when the manager is no longer needed.
     */
    public void stopPeriodicUpdate() {
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }
}
