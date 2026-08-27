import java.nio.file.Path;

/**
 * Entry point for the Downtown Gurl chatbot application.
 */
public class DowntownGurl {
    private static final Path TASK_FILE_PATH = Path.of("data", "downtownGurl.txt");
    private static final String LOAD_ERROR_MESSAGE = "Oops, I couldn't load your tasks from disk.";
    private static final String CORRUPTED_LINE_MESSAGE = "I skipped a corrupted saved task on line ";

    private final Storage storage;
    private final Ui ui;
    private final Parser parser;
    private TaskList tasks;

    /**
     * Creates the chatbot application using the given file for saved tasks.
     *
     * @param taskFilePath Path to the file used to persist tasks.
     */
    public DowntownGurl(Path taskFilePath) {
        this.ui = new Ui();
        this.storage = new Storage(taskFilePath);
        this.parser = new Parser();
        this.tasks = new TaskList();
    }

    /**
     * Starts reading and handling user commands.
     */
    public void run() {
        this.ui.showWelcome();
        this.tasks = loadTasks();
        while (this.ui.hasNextCommand()) {
            String command = this.ui.readCommand();
            try {
                if (handleCommand(this.parser.parse(command, this.tasks))) {
                    break;
                }
            } catch (DowntownGurlException e) {
                this.ui.showError(e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        new DowntownGurl(TASK_FILE_PATH).run();
    }

    /**
     * Handles one user command.
     *
     * @param command Parsed command to execute.
     * @return true if the user wants to exit, false otherwise.
     * @throws DowntownGurlException If the command cannot be handled.
     */
    private boolean handleCommand(Command command) throws DowntownGurlException {
        switch (command.getType()) {
        case BYE:
            this.ui.showGoodbye();
            return true;

        case LIST:
            this.ui.showTaskList(this.tasks);
            return false;

        case MARK:
            int taskIndex = command.getTaskIndex();
            Task task = this.tasks.get(taskIndex);
            boolean wasDone = task.isDone();
            this.tasks.markAsDone(taskIndex);
            try {
                this.storage.saveTasks(this.tasks);
            } catch (DowntownGurlException e) {
                restoreTaskStatus(task, wasDone);
                throw e;
            }
            this.ui.showUpdatedTask("Kays, I've marked this task as done!", task);
            return false;

        case UNMARK:
            int unmarkedTaskIndex = command.getTaskIndex();
            Task unmarkedTask = this.tasks.get(unmarkedTaskIndex);
            boolean wasTaskDone = unmarkedTask.isDone();
            this.tasks.markAsNotDone(unmarkedTaskIndex);
            try {
                this.storage.saveTasks(this.tasks);
            } catch (DowntownGurlException e) {
                restoreTaskStatus(unmarkedTask, wasTaskDone);
                throw e;
            }
            this.ui.showUpdatedTask("Sure, I unmarked it!", unmarkedTask);
            return false;

        case DELETE:
            int removedTaskIndex = command.getTaskIndex();
            Task removedTask = this.tasks.remove(removedTaskIndex);
            try {
                this.storage.saveTasks(this.tasks);
            } catch (DowntownGurlException e) {
                this.tasks.add(removedTaskIndex, removedTask);
                throw e;
            }
            this.ui.showDeletedTask(removedTask, this.tasks.size());
            return false;

        case ADD:
            Task addedTask = command.getTask();
            addTask(addedTask);
            this.ui.showAddedTask(addedTask, this.tasks.size());
            return false;
        }
        throw new AssertionError("Unknown command type: " + command.getType());
    }

    /**
     * Adds a task only if the updated list can be saved.
     *
     * @param addedTask Task to add.
     * @throws DowntownGurlException If the updated task list cannot be saved.
     */
    private void addTask(Task addedTask) throws DowntownGurlException {
        this.tasks.add(addedTask);
        try {
            this.storage.saveTasks(this.tasks);
        } catch (DowntownGurlException e) {
            this.tasks.removeLast();
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
     * @return Task list from the data file, or an empty list if the file does not exist.
     */
    private TaskList loadTasks() {
        try {
            TaskList loadedTasks = new TaskList(this.storage.loadTasks());
            for (int lineNumber : this.storage.getCorruptedLineNumbers()) {
                this.ui.showError(CORRUPTED_LINE_MESSAGE + lineNumber + ".");
            }
            return loadedTasks;
        } catch (DowntownGurlException e) {
            this.ui.showError(LOAD_ERROR_MESSAGE);
            return new TaskList();
        }
    }
}
