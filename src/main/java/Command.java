/**
 * Represents a user command after it has been parsed.
 */
public class Command {
    private final CommandType type;
    private final Task task;
    private final int taskIndex;

    private Command(CommandType type, Task task, int taskIndex) {
        this.type = type;
        this.task = task;
        this.taskIndex = taskIndex;
    }

    /**
     * Creates a command that does not need extra data.
     *
     * @param type Kind of command.
     * @return Parsed command.
     */
    public static Command of(CommandType type) {
        return new Command(type, null, -1);
    }

    /**
     * Creates a command that refers to an existing task.
     *
     * @param type Kind of command.
     * @param taskIndex Zero-based index of the selected task.
     * @return Parsed command.
     */
    public static Command forTaskIndex(CommandType type, int taskIndex) {
        return new Command(type, null, taskIndex);
    }

    /**
     * Creates a command that adds a new task.
     *
     * @param task New task to add.
     * @return Parsed add command.
     */
    public static Command addTask(Task task) {
        return new Command(CommandType.ADD, task, -1);
    }

    /**
     * Returns the command type.
     *
     * @return Kind of command.
     */
    public CommandType getType() {
        return this.type;
    }

    /**
     * Returns the task attached to an add command.
     *
     * @return Task to add.
     */
    public Task getTask() {
        return this.task;
    }

    /**
     * Returns the zero-based task index attached to a task-selection command.
     *
     * @return Selected task index.
     */
    public int getTaskIndex() {
        return this.taskIndex;
    }
}
