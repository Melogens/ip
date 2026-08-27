package downtowngurl.command;

import downtowngurl.storage.Storage;
import downtowngurl.task.TaskList;
import downtowngurl.ui.Ui;

/**
 * Represents the command that shows all tasks.
 */
public class ListCommand extends Command {
    @Override
    public void execute(TaskList tasks, Storage storage, Ui ui) {
        ui.showTaskList(tasks);
    }
}
