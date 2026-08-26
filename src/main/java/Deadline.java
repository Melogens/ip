import java.time.LocalDateTime;

/**
 * Represents a task that should be completed by a specific date or time.
 */
public class Deadline extends Task {
    private final LocalDateTime by;

    /**
     * Creates a deadline task.
     *
     * @param description Details of the task.
     * @param by Date and time by which the task should be completed.
     */
    public Deadline(String description, LocalDateTime by) {
        super(description, TaskType.DEADLINE);
        this.by = by;
    }

    @Override
    public String toStorageString() {
        return super.toStorageString() + " * " + escapeStorageField(TaskDateTime.formatForStorage(this.by));
    }

    @Override
    public String toString() {
        return super.toString() + " (by: " + TaskDateTime.formatForDisplay(this.by) + ")";
    }
}
