import java.nio.file.Path;

/**
 * Entry point for the Downtown Gurl chatbot application.
 */
public class DowntownGurl {
    private static final Path TASK_FILE_PATH = Path.of("data", "downtownGurl.txt");
    private static final String LOAD_ERROR_MESSAGE = "Oops, I couldn't load your tasks from disk.";
    private static final String CORRUPTED_LINE_MESSAGE = "I skipped a corrupted saved task on line ";

    public static void main(String[] args) {
        Ui ui = new Ui();
        ui.showWelcome();
        Storage storage = new Storage(TASK_FILE_PATH);
        TaskList tasks = loadTasks(storage, ui);
        Parser parser = new Parser();

        while (ui.hasNextCommand()) {
            String command = ui.readCommand();
            try {
                if (handleCommand(parser.parse(command, tasks), tasks, storage, ui)) {
                    break;
                }
            } catch (DowntownGurlException e) {
                ui.showError(e.getMessage());
            }
        }
    }

    /**
     * Handles one user command.
     *
     * @param command Parsed command to execute.
     * @param tasks Current task list.
     * @param storage Storage helper used to save task changes.
     * @param ui UI helper used to show command results.
     * @return true if the user wants to exit, false otherwise.
     * @throws DowntownGurlException If the command cannot be handled.
     */
    private static boolean handleCommand(Command command, TaskList tasks, Storage storage, Ui ui)
            throws DowntownGurlException {
        switch (command.getType()) {
        case BYE:
            ui.showGoodbye();
            return true;

        case LIST:
            ui.showTaskList(tasks);
            return false;

        case MARK:
            int taskIndex = command.getTaskIndex();
            Task task = tasks.get(taskIndex);
            boolean wasDone = task.isDone();
            tasks.markAsDone(taskIndex);
            try {
                storage.saveTasks(tasks);
            } catch (DowntownGurlException e) {
                restoreTaskStatus(task, wasDone);
                throw e;
            }
            ui.showUpdatedTask("Kays, I've marked this task as done!", task);
            return false;

        case UNMARK:
            int unmarkedTaskIndex = command.getTaskIndex();
            Task unmarkedTask = tasks.get(unmarkedTaskIndex);
            boolean wasTaskDone = unmarkedTask.isDone();
            tasks.markAsNotDone(unmarkedTaskIndex);
            try {
                storage.saveTasks(tasks);
            } catch (DowntownGurlException e) {
                restoreTaskStatus(unmarkedTask, wasTaskDone);
                throw e;
            }
            ui.showUpdatedTask("Sure, I unmarked it!", unmarkedTask);
            return false;

        case DELETE:
            int removedTaskIndex = command.getTaskIndex();
            Task removedTask = tasks.remove(removedTaskIndex);
            try {
                storage.saveTasks(tasks);
            } catch (DowntownGurlException e) {
                tasks.add(removedTaskIndex, removedTask);
                throw e;
            }
            ui.showDeletedTask(removedTask, tasks.size());
            return false;

        case ADD:
            Task addedTask = command.getTask();
            addTask(tasks, addedTask, storage);
            ui.showAddedTask(addedTask, tasks.size());
            return false;
        }
        throw new AssertionError("Unknown command type: " + command.getType());
    }

    /**
     * Adds a task only if the updated list can be saved.
     *
     * @param tasks Current task list.
     * @param addedTask Task to add.
     * @param storage Storage helper used to save the updated task list.
     * @throws DowntownGurlException If the updated task list cannot be saved.
     */
    private static void addTask(TaskList tasks, Task addedTask, Storage storage) throws DowntownGurlException {
        tasks.add(addedTask);
        try {
            storage.saveTasks(tasks);
        } catch (DowntownGurlException e) {
            tasks.removeLast();
            throw e;
        }
    }

    /**
     * Restores a task's done status after a failed save.
     *
     * @param task Task to restore.
     * @param wasDone Previous done status.
     */
    private static void restoreTaskStatus(Task task, boolean wasDone) {
        if (wasDone) {
            task.markAsDone();
        } else {
            task.markAsNotDone();
        }
    }

    /**
     * Loads tasks from the data file if it already exists.
     *
     * @param storage Storage helper used to load saved tasks.
     * @param ui UI helper used to show load warnings.
     * @return Task list from the data file, or an empty list if the file does not exist.
     */
    private static TaskList loadTasks(Storage storage, Ui ui) {
        try {
            TaskList tasks = new TaskList(storage.loadTasks());
            for (int lineNumber : storage.getCorruptedLineNumbers()) {
                ui.showError(CORRUPTED_LINE_MESSAGE + lineNumber + ".");
            }
            return tasks;
        } catch (DowntownGurlException e) {
            ui.showError(LOAD_ERROR_MESSAGE);
            return new TaskList();
        }
    }
}
