import java.nio.file.Path;
import java.util.ArrayList;

/**
 * Entry point for the Downtown Gurl chatbot application.
 */
public class DowntownGurl {
    private static final String DEADLINE_SEPARATOR = " /by ";
    private static final String EVENT_FROM_SEPARATOR = " /from ";
    private static final String EVENT_TO_SEPARATOR = " /to ";
    private static final Path TASK_FILE_PATH = Path.of("data", "downtownGurl.txt");
    private static final String EMPTY_TASK_MESSAGE = "Soz queen you gotta at least give me SOMETHING to work with.";
    private static final String DEADLINE_FORMAT_HINT = EMPTY_TASK_MESSAGE
            + "\nMaybe you could try formatting it as: deadline <task name> /by dd/mm/yyyy time";
    private static final String EVENT_FORMAT_HINT = EMPTY_TASK_MESSAGE
            + "\nMaybe you could try formatting it as: event <event name> /from dd/mm/yyyy time /to dd/mm/yyyy time";
    private static final String UNKNOWN_COMMAND_MESSAGE = "U sleeping alright? Sounds like you ain't...";
    private static final String LOAD_ERROR_MESSAGE = "Oops, I couldn't load your tasks from disk.";
    private static final String CORRUPTED_LINE_MESSAGE = "I skipped a corrupted saved task on line ";

    public static void main(String[] args) {
        Ui ui = new Ui();
        ui.showWelcome();
        Storage storage = new Storage(TASK_FILE_PATH);
        ArrayList<Task> tasks = loadTasks(storage, ui);

        while (ui.hasNextCommand()) {
            String command = ui.readCommand();
            try {
                if (handleCommand(command, tasks, storage, ui)) {
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
     * @param command Full user command.
     * @param tasks Current task list.
     * @param storage Storage helper used to save task changes.
     * @param ui UI helper used to show command results.
     * @return true if the user wants to exit, false otherwise.
     * @throws DowntownGurlException If the command cannot be handled.
     */
    private static boolean handleCommand(String command, ArrayList<Task> tasks, Storage storage, Ui ui)
            throws DowntownGurlException {
        if (command.equals("bye")) {
            ui.showGoodbye();
            return true;
        }

        if (command.equals("list")) {
            ui.showTaskList(tasks);
            return false;
        }

        if (command.startsWith("mark ")) {
            Task task = getTaskByNumberFromCommand(tasks, command, 5);
            boolean wasDone = task.isDone();
            task.markAsDone();
            try {
                storage.saveTasks(tasks);
            } catch (DowntownGurlException e) {
                restoreTaskStatus(task, wasDone);
                throw e;
            }
            ui.showUpdatedTask("Kays, I've marked this task as done!", task);
            return false;
        }

        if (command.startsWith("unmark ")) {
            Task task = getTaskByNumberFromCommand(tasks, command, 7);
            boolean wasDone = task.isDone();
            task.markAsNotDone();
            try {
                storage.saveTasks(tasks);
            } catch (DowntownGurlException e) {
                restoreTaskStatus(task, wasDone);
                throw e;
            }
            ui.showUpdatedTask("Sure, I unmarked it!", task);
            return false;
        }

        if (command.startsWith("delete ")) {
            int taskIndex = getTaskIndexFromCommand(tasks, command, 7);
            Task removedTask = tasks.remove(taskIndex);
            try {
                storage.saveTasks(tasks);
            } catch (DowntownGurlException e) {
                tasks.add(taskIndex, removedTask);
                throw e;
            }
            ui.showDeletedTask(removedTask, tasks.size());
            return false;
        }

        if (command.equals("todo") || command.startsWith("todo ")) {
            Task addedTask = createTodo(command);
            addTask(tasks, addedTask, storage);
            ui.showAddedTask(addedTask, tasks.size());
            return false;
        }

        if (command.equals("deadline") || command.startsWith("deadline ")) {
            Task addedTask = createDeadline(command);
            addTask(tasks, addedTask, storage);
            ui.showAddedTask(addedTask, tasks.size());
            return false;
        }

        if (command.equals("event") || command.startsWith("event ")) {
            Task addedTask = createEvent(command);
            addTask(tasks, addedTask, storage);
            ui.showAddedTask(addedTask, tasks.size());
            return false;
        }

        throw new DowntownGurlException(UNKNOWN_COMMAND_MESSAGE);
    }

    /**
     * Creates a todo task from a command in this form: todo DESCRIPTION.
     *
     * @param command Full user command.
     * @return New todo task.
     * @throws DowntownGurlException If the todo description is missing.
     */
    private static Todo createTodo(String command) throws DowntownGurlException {
        if (command.length() <= 5 || command.substring(5).isBlank()) {
            throw new DowntownGurlException(EMPTY_TASK_MESSAGE);
        }
        return new Todo(command.substring(5));
    }

    /**
     * Creates a deadline task from a command in this form: deadline DESCRIPTION /by TIME.
     *
     * @param command Full user command.
     * @return New deadline task.
     * @throws DowntownGurlException If the description or deadline time is missing.
     */
    private static Deadline createDeadline(String command) throws DowntownGurlException {
        int separatorIndex = command.indexOf(DEADLINE_SEPARATOR);
        if (separatorIndex == -1) {
            throw new DowntownGurlException(DEADLINE_FORMAT_HINT);
        }
        String description = command.substring(9, separatorIndex);
        String by = command.substring(separatorIndex + DEADLINE_SEPARATOR.length());
        if (description.isBlank() || by.isBlank()) {
            throw new DowntownGurlException(DEADLINE_FORMAT_HINT);
        }
        try {
            return new Deadline(description, TaskDateTime.parse(by));
        } catch (DowntownGurlException e) {
            throw new DowntownGurlException(DEADLINE_FORMAT_HINT);
        }
    }

    /**
     * Creates an event task from a command in this form: event DESCRIPTION /from START /to END.
     *
     * @param command Full user command.
     * @return New event task.
     * @throws DowntownGurlException If the description, start, or end is missing.
     */
    private static Event createEvent(String command) throws DowntownGurlException {
        int fromIndex = command.indexOf(EVENT_FROM_SEPARATOR);
        int toIndex = command.indexOf(EVENT_TO_SEPARATOR);
        if (fromIndex == -1 || toIndex == -1 || fromIndex >= toIndex) {
            throw new DowntownGurlException(EVENT_FORMAT_HINT);
        }
        String description = command.substring(6, fromIndex);
        String from = command.substring(fromIndex + EVENT_FROM_SEPARATOR.length(), toIndex);
        String to = command.substring(toIndex + EVENT_TO_SEPARATOR.length());
        if (description.isBlank() || from.isBlank() || to.isBlank()) {
            throw new DowntownGurlException(EVENT_FORMAT_HINT);
        }
        try {
            return new Event(description, TaskDateTime.parse(from), TaskDateTime.parse(to));
        } catch (DowntownGurlException e) {
            throw new DowntownGurlException(EVENT_FORMAT_HINT);
        }
    }

    /**
     * Finds the task number in a command and returns the matching task.
     *
     * @param tasks Current task list.
     * @param command Full user command.
     * @param taskNumberStartIndex Index where the task number starts.
     * @return Task selected by the user.
     * @throws DowntownGurlException If the task number is not valid.
     */
    private static Task getTaskByNumberFromCommand(ArrayList<Task> tasks, String command, int taskNumberStartIndex)
            throws DowntownGurlException {
        return tasks.get(getTaskIndexFromCommand(tasks, command, taskNumberStartIndex));
    }

    /**
     * Finds the zero-based task index in a command containing a one-based task number.
     *
     * @param tasks Current task list.
     * @param command Full user command.
     * @param taskNumberStartIndex Index where the task number starts.
     * @return Zero-based index of the task selected by the user.
     * @throws DowntownGurlException If the task number is not valid.
     */
    private static int getTaskIndexFromCommand(ArrayList<Task> tasks, String command, int taskNumberStartIndex)
            throws DowntownGurlException {
        int taskNumber;
        try {
            taskNumber = Integer.parseInt(command.substring(taskNumberStartIndex).trim());
        } catch (NumberFormatException e) {
            throw new DowntownGurlException(UNKNOWN_COMMAND_MESSAGE);
        }
        if (taskNumber < 1 || taskNumber > tasks.size()) {
            throw new DowntownGurlException(UNKNOWN_COMMAND_MESSAGE);
        }
        return taskNumber - 1;
    }

    /**
     * Adds a task only if the updated list can be saved.
     *
     * @param tasks Current task list.
     * @param addedTask Task to add.
     * @param storage Storage helper used to save the updated task list.
     * @throws DowntownGurlException If the updated task list cannot be saved.
     */
    private static void addTask(ArrayList<Task> tasks, Task addedTask, Storage storage) throws DowntownGurlException {
        tasks.add(addedTask);
        try {
            storage.saveTasks(tasks);
        } catch (DowntownGurlException e) {
            tasks.remove(tasks.size() - 1);
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
    private static ArrayList<Task> loadTasks(Storage storage, Ui ui) {
        try {
            ArrayList<Task> tasks = storage.loadTasks();
            for (int lineNumber : storage.getCorruptedLineNumbers()) {
                ui.showError(CORRUPTED_LINE_MESSAGE + lineNumber + ".");
            }
            return tasks;
        } catch (DowntownGurlException e) {
            ui.showError(LOAD_ERROR_MESSAGE);
            return new ArrayList<>();
        }
    }
}
