package downtowngurl.command;

import downtowngurl.exception.DowntownGurlException;
import downtowngurl.storage.Storage;
import downtowngurl.task.Task;
import downtowngurl.task.TaskList;
import downtowngurl.ui.Ui;

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
