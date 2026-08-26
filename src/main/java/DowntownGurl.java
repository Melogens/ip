import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Entry point for the Downtown Gurl chatbot application.
 */
public class DowntownGurl {
    private static final String CHATBOT_NAME = "Downtown Gurl";
    private static final String DIVIDER = "<*><*><*><*><*><*><*><*><*><*><*><*><*><*><*><*><*><*><*><*><*><*><*><*>";
    private static final String DEADLINE_SEPARATOR = " /by ";
    private static final String EVENT_FROM_SEPARATOR = " /from ";
    private static final String EVENT_TO_SEPARATOR = " /to ";
    private static final Path TASK_FILE_PATH = Path.of("data", "downtownGurl.txt");
    private static final String STORAGE_SEPARATOR = " * ";
    private static final String STORAGE_DONE_STATUS = "Done";
    private static final String STORAGE_NOT_DONE_STATUS = "Not done";
    private static final String EMPTY_TASK_MESSAGE = "Soz queen you gotta at least give me SOMETHING to work with.";
    private static final String UNKNOWN_COMMAND_MESSAGE = "U sleeping alright? Sounds like you ain't...";
    private static final String SAVE_ERROR_MESSAGE = "Oops, I couldn't save your tasks to disk.";
    private static final String LOAD_ERROR_MESSAGE = "Oops, I couldn't load your tasks from disk.";

    public static void main(String[] args) {
        String banner = """
                 ____                      _                       ____           __\s
                |  _ \\  _____      ___ __ | |_ _____      ___ __  / ___|_   _ _ __| |
                | | | |/ _ \\ \\ /\\ / / '_ \\| __/ _ \\ \\ /\\ / / '_ \\| |___| | | ' __|| |
                | |_| | (_) \\ V  V /| | | | || (_) \\ V  V /| | | | |_| | |_| | |  | |
                |____/ \\___/ \\_/\\_/ |_| |_|\\__\\___/ \\_/\\_/ |_| |_|\\____|\\__,_|_|  |_|""";
        System.out.println(DIVIDER);
        System.out.println(banner);
        System.out.println(DIVIDER);
        System.out.println("Hey I'm " + CHATBOT_NAME + ".");
        System.out.println("I'm here to give you a reality check " +
                "and help you manifest that life you've been dreaming.");
        System.out.println(DIVIDER);
        System.out.println("Darling what's up?");

        ArrayList<Task> tasks = loadTasks();

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String command = scanner.nextLine();
            try {
                if (handleCommand(command, tasks)) {
                    break;
                }
            } catch (DowntownGurlException e) {
                printErrorMessage(e.getMessage());
            }
        }
    }

    /**
     * Handles one user command.
     *
     * @param command Full user command.
     * @param tasks Current task list.
     * @return true if the user wants to exit, false otherwise.
     * @throws DowntownGurlException If the command cannot be handled.
     */
    private static boolean handleCommand(String command, ArrayList<Task> tasks) throws DowntownGurlException {
        if (command.equals("bye")) {
            System.out.println("That's bombz. Byes!");
            System.out.println(DIVIDER);
            return true;
        }

        if (command.equals("list")) {
            printTaskList(tasks);
            return false;
        }

        if (command.startsWith("mark ")) {
            Task task = getTaskByNumberFromCommand(tasks, command, 5);
            task.markAsDone();
            saveTasks(tasks);
            printUpdatedTaskMessage("Kays, I've marked this task as done!", task);
            return false;
        }

        if (command.startsWith("unmark ")) {
            Task task = getTaskByNumberFromCommand(tasks, command, 7);
            task.markAsNotDone();
            saveTasks(tasks);
            printUpdatedTaskMessage("Sure, I unmarked it!", task);
            return false;
        }

        if (command.startsWith("delete ")) {
            int taskIndex = getTaskIndexFromCommand(tasks, command, 7);
            Task removedTask = tasks.remove(taskIndex);
            saveTasks(tasks);
            printDeletedTaskMessage(removedTask, tasks.size());
            return false;
        }

        if (command.equals("todo") || command.startsWith("todo ")) {
            tasks.add(createTodo(command));
            saveTasks(tasks);
            printAddedTaskMessage(tasks.getLast(), tasks.size());
            return false;
        }

        if (command.equals("deadline") || command.startsWith("deadline ")) {
            tasks.add(createDeadline(command));
            saveTasks(tasks);
            printAddedTaskMessage(tasks.getLast(), tasks.size());
            return false;
        }

        if (command.equals("event") || command.startsWith("event ")) {
            tasks.add(createEvent(command));
            saveTasks(tasks);
            printAddedTaskMessage(tasks.getLast(), tasks.size());
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
            throw new DowntownGurlException(EMPTY_TASK_MESSAGE);
        }
        String description = command.substring(9, separatorIndex);
        String by = command.substring(separatorIndex + DEADLINE_SEPARATOR.length());
        if (description.isBlank() || by.isBlank()) {
            throw new DowntownGurlException(EMPTY_TASK_MESSAGE);
        }
        return new Deadline(description, by);
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
            throw new DowntownGurlException(EMPTY_TASK_MESSAGE);
        }
        String description = command.substring(6, fromIndex);
        String from = command.substring(fromIndex + EVENT_FROM_SEPARATOR.length(), toIndex);
        String to = command.substring(toIndex + EVENT_TO_SEPARATOR.length());
        if (description.isBlank() || from.isBlank() || to.isBlank()) {
            throw new DowntownGurlException(EMPTY_TASK_MESSAGE);
        }
        return new Event(description, from, to);
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
     * Loads tasks from the data file if it already exists.
     *
     * @return Task list from the data file, or an empty list if the file does not exist.
     */
    private static ArrayList<Task> loadTasks() {
        ArrayList<Task> tasks = new ArrayList<>();
        if (!Files.exists(TASK_FILE_PATH)) {
            return tasks;
        }

        try {
            for (String taskLine : Files.readAllLines(TASK_FILE_PATH)) {
                tasks.add(createTaskFromStorageLine(taskLine));
            }
        } catch (IOException | DowntownGurlException e) {
            printErrorMessage(LOAD_ERROR_MESSAGE);
        }
        return tasks;
    }

    /**
     * Creates a task from one line in the data file.
     *
     * @param taskLine One saved task line.
     * @return Task represented by the saved line.
     * @throws DowntownGurlException If the saved line is not in the expected format.
     */
    private static Task createTaskFromStorageLine(String taskLine) throws DowntownGurlException {
        String[] parts = taskLine.split("\\Q" + STORAGE_SEPARATOR + "\\E", 3);
        if (parts.length != 3) {
            throw new DowntownGurlException(LOAD_ERROR_MESSAGE);
        }

        Task task = switch (parts[0]) {
        case "T" -> new Todo(parts[2]);
        case "D" -> createDeadlineFromStorageDetails(parts[2]);
        case "E" -> createEventFromStorageDetails(parts[2]);
        default -> throw new DowntownGurlException(LOAD_ERROR_MESSAGE);
        };

        if (parts[1].equals(STORAGE_DONE_STATUS)) {
            task.markAsDone();
        } else if (!parts[1].equals(STORAGE_NOT_DONE_STATUS)) {
            throw new DowntownGurlException(LOAD_ERROR_MESSAGE);
        }
        return task;
    }

    /**
     * Creates a deadline from saved details in this form: DESCRIPTION, BY.
     *
     * @param details Saved deadline details.
     * @return Deadline represented by the saved details.
     * @throws DowntownGurlException If the details are not in the expected format.
     */
    private static Deadline createDeadlineFromStorageDetails(String details) throws DowntownGurlException {
        int separatorIndex = details.indexOf(", ");
        if (separatorIndex == -1) {
            throw new DowntownGurlException(LOAD_ERROR_MESSAGE);
        }
        return new Deadline(details.substring(0, separatorIndex), details.substring(separatorIndex + 2));
    }

    /**
     * Creates an event from saved details in this form: DESCRIPTION, FROM-TO.
     *
     * @param details Saved event details.
     * @return Event represented by the saved details.
     * @throws DowntownGurlException If the details are not in the expected format.
     */
    private static Event createEventFromStorageDetails(String details) throws DowntownGurlException {
        int detailsSeparatorIndex = details.indexOf(", ");
        int timeSeparatorIndex = details.indexOf("-", detailsSeparatorIndex + 2);
        if (detailsSeparatorIndex == -1 || timeSeparatorIndex == -1) {
            throw new DowntownGurlException(LOAD_ERROR_MESSAGE);
        }
        String description = details.substring(0, detailsSeparatorIndex);
        String from = details.substring(detailsSeparatorIndex + 2, timeSeparatorIndex);
        String to = details.substring(timeSeparatorIndex + 1);
        return new Event(description, from, to);
    }

    /**
     * Saves all current tasks to the data file.
     *
     * @param tasks Current task list.
     * @throws DowntownGurlException If the data file cannot be written.
     */
    private static void saveTasks(ArrayList<Task> tasks) throws DowntownGurlException {
        ArrayList<String> taskLines = new ArrayList<>();
        for (Task task : tasks) {
            taskLines.add(task.toStorageString());
        }

        try {
            Files.createDirectories(TASK_FILE_PATH.getParent());
            Files.write(TASK_FILE_PATH, taskLines);
        } catch (IOException e) {
            throw new DowntownGurlException(SAVE_ERROR_MESSAGE);
        }
    }

    /**
     * Prints all tasks currently in the list.
     *
     * @param tasks Current task list.
     */
    private static void printTaskList(ArrayList<Task> tasks) {
        System.out.println("Here's your tasks:");
        for (int i = 0; i < tasks.size(); i++) {
            System.out.println(" " + (i + 1) + ". " + tasks.get(i));
        }
        System.out.println(DIVIDER);
    }

    /**
     * Prints the result of marking or unmarking a task.
     *
     * @param message Confirmation message.
     * @param task Updated task.
     */
    private static void printUpdatedTaskMessage(String message, Task task) {
        System.out.println(message);
        System.out.println("  " + task);
        System.out.println(DIVIDER);
    }

    /**
     * Prints a confirmation for the newly added task.
     *
     * @param addedTask Task that was added.
     * @param taskCount Number of tasks after adding the task.
     */
    private static void printAddedTaskMessage(Task addedTask, int taskCount) {
        System.out.println("Gotcha. Noted it downz:");
        System.out.println("  " + addedTask);
        System.out.println("Now you got " + taskCount + " tasks in the roster.");
        System.out.println(DIVIDER);
    }

    /**
     * Prints a confirmation for the deleted task.
     *
     * @param removedTask Task that was removed.
     * @param taskCount Number of tasks after removing the task.
     */
    private static void printDeletedTaskMessage(Task removedTask, int taskCount) {
        System.out.println("Sure~ I've removed this task:");
        System.out.println("  " + removedTask);
        System.out.println("Now you got " + taskCount + " tasks in the list.");
        System.out.println(DIVIDER);
    }

    /**
     * Prints an error message using the same divider style as normal chatbot replies.
     *
     * @param message Error message to show.
     */
    private static void printErrorMessage(String message) {
        System.out.println(message);
        System.out.println(DIVIDER);
    }
}
