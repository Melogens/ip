/**
 * Represents a task that happens between a start date or time and an end date or time.
 */
public class Event extends Task {
    private final String from;
    private final String to;

    /**
     * Creates an event task.
     *
     * @param description Details of the task.
     * @param from Date or time when the event starts.
     * @param to Date or time when the event ends.
     */
    public Event(String description, String from, String to) {
        super(description, TaskType.EVENT);
        this.from = from;
        this.to = to;
    }

    @Override
    public String toStorageString() {
        return super.toStorageString() + ", " + this.from + "-" + this.to;
    }

    @Override
    public String toString() {
        return super.toString() + " (from: " + this.from + " to: " + this.to + ")";
    }
}
