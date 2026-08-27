/**
 * Represents a user command after it has been parsed.
 */
public abstract class Command {
    /**
     * Executes this command.
     *
     * @param tasks Current task list.
     * @param storage Storage helper used to save task changes.
     * @param ui UI helper used to show command results.
     * @throws DowntownGurlException If the command cannot be completed.
     */
    public abstract void execute(TaskList tasks, Storage storage, Ui ui) throws DowntownGurlException;

    /**
     * Returns whether this command should exit the application after execution.
     *
     * @return true if the application should exit, false otherwise.
     */
    public boolean isExit() {
        return false;
    }

    /**
     * Restores a task's done status after a failed save.
     *
     * @param task Task to restore.
     * @param wasDone Previous done status.
     */
    protected void restoreTaskStatus(Task task, boolean wasDone) {
        if (wasDone) {
            task.markAsDone();
        } else {
            task.markAsNotDone();
        }
    }

    /**
     * Checks that a parsed task index exists in the current task list.
     *
     * @param tasks Current task list.
     * @param taskIndex Zero-based task index to check.
     * @throws DowntownGurlException If the task index does not exist.
     */
    protected void requireValidTaskIndex(TaskList tasks, int taskIndex) throws DowntownGurlException {
        if (taskIndex < 0 || taskIndex >= tasks.size()) {
            throw new DowntownGurlException("U sleeping alright? Sounds like you ain't...");
        }
    }
}

/**
 * Represents the command that exits the chatbot.
 */
class ByeCommand extends Command {
    @Override
    public void execute(TaskList tasks, Storage storage, Ui ui) {
        ui.showGoodbye();
    }

    @Override
    public boolean isExit() {
        return true;
    }
}

/**
 * Represents the command that shows all tasks.
 */
class ListCommand extends Command {
    @Override
    public void execute(TaskList tasks, Storage storage, Ui ui) {
        ui.showTaskList(tasks);
    }
}

/**
 * Represents the command that marks a task as done.
 */
class MarkCommand extends Command {
    private final int taskIndex;

    /**
     * Creates a command for marking the selected task.
     *
     * @param taskIndex Zero-based index of the task to mark.
     */
    MarkCommand(int taskIndex) {
        this.taskIndex = taskIndex;
    }

    @Override
    public void execute(TaskList tasks, Storage storage, Ui ui) throws DowntownGurlException {
        requireValidTaskIndex(tasks, this.taskIndex);
        Task task = tasks.get(this.taskIndex);
        boolean wasDone = task.isDone();
        tasks.markAsDone(this.taskIndex);
        try {
            storage.saveTasks(tasks);
        } catch (DowntownGurlException e) {
            restoreTaskStatus(task, wasDone);
            throw e;
        }
        ui.showUpdatedTask("Kays, I've marked this task as done!", task);
    }
}

/**
 * Represents the command that marks a task as not done.
 */
class UnmarkCommand extends Command {
    private final int taskIndex;

    /**
     * Creates a command for unmarking the selected task.
     *
     * @param taskIndex Zero-based index of the task to unmark.
     */
    UnmarkCommand(int taskIndex) {
        this.taskIndex = taskIndex;
    }

    @Override
    public void execute(TaskList tasks, Storage storage, Ui ui) throws DowntownGurlException {
        requireValidTaskIndex(tasks, this.taskIndex);
        Task task = tasks.get(this.taskIndex);
        boolean wasDone = task.isDone();
        tasks.markAsNotDone(this.taskIndex);
        try {
            storage.saveTasks(tasks);
        } catch (DowntownGurlException e) {
            restoreTaskStatus(task, wasDone);
            throw e;
        }
        ui.showUpdatedTask("Sure, I unmarked it!", task);
    }
}

/**
 * Represents the command that deletes a task.
 */
class DeleteCommand extends Command {
    private final int taskIndex;

    /**
     * Creates a command for deleting the selected task.
     *
     * @param taskIndex Zero-based index of the task to delete.
     */
    DeleteCommand(int taskIndex) {
        this.taskIndex = taskIndex;
    }

    @Override
    public void execute(TaskList tasks, Storage storage, Ui ui) throws DowntownGurlException {
        requireValidTaskIndex(tasks, this.taskIndex);
        Task removedTask = tasks.remove(this.taskIndex);
        try {
            storage.saveTasks(tasks);
        } catch (DowntownGurlException e) {
            tasks.add(this.taskIndex, removedTask);
            throw e;
        }
        ui.showDeletedTask(removedTask, tasks.size());
    }
}

/**
 * Represents the command that adds a new task.
 */
class AddCommand extends Command {
    private final Task task;

    /**
     * Creates a command for adding the given task.
     *
     * @param task Task to add.
     */
    AddCommand(Task task) {
        this.task = task;
    }

    @Override
    public void execute(TaskList tasks, Storage storage, Ui ui) throws DowntownGurlException {
        tasks.add(this.task);
        try {
            storage.saveTasks(tasks);
        } catch (DowntownGurlException e) {
            tasks.removeLast();
            throw e;
        }
        ui.showAddedTask(this.task, tasks.size());
    }
}
