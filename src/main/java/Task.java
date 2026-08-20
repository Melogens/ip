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

    @Override
    public String toString() {
        return "[" + getStatusIcon() + "] " + this.description;
    }
}
