package downtowngurl.command;

import downtowngurl.exception.DowntownGurlException;
import downtowngurl.storage.Storage;
import downtowngurl.task.Task;
import downtowngurl.task.TaskList;
import downtowngurl.ui.Ui;

/**
 * Represents the command that adds a new task.
 */
public class AddCommand extends Command {
    private final Task task;

    /**
     * Creates a command for adding the given task.
     *
     * @param task Task to add.
     */
    public AddCommand(Task task) {
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
