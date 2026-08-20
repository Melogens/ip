/**
 * Represents one task tracked by the chatbot.
 */
public class Task {
    private final String description;
    private boolean isDone;

    /**
     * Creates a new task that has not been marked as done yet.
     *
     * @param description Details of the task.
     */
    public Task(String description) {
        this.description = description;
        this.isDone = false;
    }

    /**
     * Marks this task as done.
     */
    public void markAsDone() {
        this.isDone = true;
    }

    /**
     * Marks this task as not done.
     */
    public void markAsNotDone() {
        this.isDone = false;
    }

    /**
     * Returns the icon used to show whether this task is done.
     *
     * @return X if the task is done, or a blank space otherwise.
     */
    public String getStatusIcon() {
        return this.isDone ? "X" : " ";
    }

    /**
     * Returns the symbol used to identify this task type.
     *
     * @return Task type symbol.
     */
    protected String getTypeIcon() {
        return " ";
    }

    /**
     * Returns the user-provided details of this task.
     *
     * @return Task description.
     */
    protected String getDescription() {
        return this.description;
    }

    @Override
    public String toString() {
        return "[" + getTypeIcon() + "][" + getStatusIcon() + "] " + this.description;
    }
}
