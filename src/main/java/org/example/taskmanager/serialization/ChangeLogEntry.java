package org.example.taskmanager.serialization;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Represents an entry in a change log, recording modifications to an entity.
 * This class is serializable to allow for persistence of change log entries.
 */
public class ChangeLogEntry implements Serializable {
    private final String fieldChanged;
    private final String oldValue;
    private final String newValue;
    private final String changedByRole;
    private final LocalDateTime timestamp;

    /**
     * Constructs a new ChangeLogEntry with the specified details.
     * The timestamp is automatically set to the current time, truncated to seconds.
     *
     * @param fieldChanged  the name of the field that was changed
     * @param oldValue      the previous value of the field
     * @param newValue      the new value of the field
     * @param changedByRole the role of the user who made the change
     */
    public ChangeLogEntry(String fieldChanged, String oldValue, String newValue, String changedByRole) {
        this.fieldChanged = fieldChanged;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.changedByRole = changedByRole;
        this.timestamp = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }

    public String getFieldChanged() {
        return fieldChanged;
    }

    public String getOldValue() {
        return oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public String getChangedByRole() {
        return changedByRole;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
