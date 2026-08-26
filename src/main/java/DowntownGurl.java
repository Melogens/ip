import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
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
    private static final String DEADLINE_FORMAT_HINT = EMPTY_TASK_MESSAGE
            + "\nMaybe you could try formatting it as: deadline <task name> /by dd/mm/yyyy time";
    private static final String EVENT_FORMAT_HINT = EMPTY_TASK_MESSAGE
            + "\nMaybe you could try formatting it as: event <event name> /from dd/mm/yyyy time /to dd/mm/yyyy time";
    private static final String UNKNOWN_COMMAND_MESSAGE = "U sleeping alright? Sounds like you ain't...";
    private static final String SAVE_ERROR_MESSAGE = "Oops, I couldn't save your tasks to disk.";
    private static final String LOAD_ERROR_MESSAGE = "Oops, I couldn't load your tasks from disk.";
    private static final String CORRUPTED_LINE_MESSAGE = "I skipped a corrupted saved task on line ";

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
            boolean wasDone = task.isDone();
            task.markAsDone();
            try {
                saveTasks(tasks);
            } catch (DowntownGurlException e) {
                restoreTaskStatus(task, wasDone);
                throw e;
            }
            printUpdatedTaskMessage("Kays, I've marked this task as done!", task);
            return false;
        }

        if (command.startsWith("unmark ")) {
            Task task = getTaskByNumberFromCommand(tasks, command, 7);
            boolean wasDone = task.isDone();
            task.markAsNotDone();
            try {
                saveTasks(tasks);
            } catch (DowntownGurlException e) {
                restoreTaskStatus(task, wasDone);
                throw e;
            }
            printUpdatedTaskMessage("Sure, I unmarked it!", task);
            return false;
        }

        if (command.startsWith("delete ")) {
            int taskIndex = getTaskIndexFromCommand(tasks, command, 7);
            Task removedTask = tasks.remove(taskIndex);
            try {
                saveTasks(tasks);
            } catch (DowntownGurlException e) {
                tasks.add(taskIndex, removedTask);
                throw e;
            }
            printDeletedTaskMessage(removedTask, tasks.size());
            return false;
        }

        if (command.equals("todo") || command.startsWith("todo ")) {
            Task addedTask = createTodo(command);
            addTask(tasks, addedTask);
            printAddedTaskMessage(addedTask, tasks.size());
            return false;
        }

        if (command.equals("deadline") || command.startsWith("deadline ")) {
            Task addedTask = createDeadline(command);
            addTask(tasks, addedTask);
            printAddedTaskMessage(addedTask, tasks.size());
            return false;
        }

        if (command.equals("event") || command.startsWith("event ")) {
            Task addedTask = createEvent(command);
            addTask(tasks, addedTask);
            printAddedTaskMessage(addedTask, tasks.size());
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
     * @throws DowntownGurlException If the updated task list cannot be saved.
     */
    private static void addTask(ArrayList<Task> tasks, Task addedTask) throws DowntownGurlException {
        tasks.add(addedTask);
        try {
            saveTasks(tasks);
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
     * @return Task list from the data file, or an empty list if the file does not exist.
     */
    private static ArrayList<Task> loadTasks() {
        ArrayList<Task> tasks = new ArrayList<>();
        if (!Files.exists(TASK_FILE_PATH)) {
            return tasks;
        }

        try {
            ArrayList<String> taskLines = new ArrayList<>(Files.readAllLines(TASK_FILE_PATH));
            for (int i = 0; i < taskLines.size(); i++) {
                String taskLine = taskLines.get(i);
                if (taskLine.isBlank()) {
                    continue;
                }
                try {
                    tasks.add(createTaskFromStorageLine(taskLine));
                } catch (DowntownGurlException e) {
                    printErrorMessage(CORRUPTED_LINE_MESSAGE + (i + 1) + ".");
                }
            }
        } catch (IOException e) {
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
        String[] parts = taskLine.split("\\Q" + STORAGE_SEPARATOR + "\\E", -1);
        if (parts.length < 3 || parts[0].isBlank() || parts[1].isBlank()) {
            throw new DowntownGurlException(LOAD_ERROR_MESSAGE);
        }

        Task task = switch (parts[0]) {
        case "T" -> createTodoFromStorageParts(parts);
        case "D" -> createDeadlineFromStorageParts(parts);
        case "E" -> createEventFromStorageParts(parts);
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
     * Creates a todo from saved fields.
     *
     * @param parts Saved task fields.
     * @return Todo represented by the saved fields.
     * @throws DowntownGurlException If the fields are not in the expected format.
     */
    private static Todo createTodoFromStorageParts(String[] parts) throws DowntownGurlException {
        if (parts.length != 3 || parts[2].isBlank()) {
            throw new DowntownGurlException(LOAD_ERROR_MESSAGE);
        }
        return new Todo(unescapeStorageField(parts[2]));
    }

    /**
     * Creates a deadline from saved fields.
     *
     * @param parts Saved task fields.
     * @return Deadline represented by the saved fields.
     * @throws DowntownGurlException If the fields are not in the expected format.
     */
    private static Deadline createDeadlineFromStorageParts(String[] parts) throws DowntownGurlException {
        if (parts.length == 4) {
            String description = unescapeStorageField(parts[2]);
            String by = unescapeStorageField(parts[3]);
            if (description.isBlank() || by.isBlank()) {
                throw new DowntownGurlException(LOAD_ERROR_MESSAGE);
            }
            return new Deadline(description, TaskDateTime.parseFromStorage(by));
        }
        if (parts.length != 3 || parts[2].isBlank()) {
            throw new DowntownGurlException(LOAD_ERROR_MESSAGE);
        }
        return createDeadlineFromOldStorageDetails(parts[2]);
    }

    /**
     * Creates a deadline from old saved details in this form: DESCRIPTION, BY.
     *
     * @param details Old saved deadline details.
     * @return Deadline represented by the old saved details.
     * @throws DowntownGurlException If the old details are not in the expected format.
     */
    private static Deadline createDeadlineFromOldStorageDetails(String details) throws DowntownGurlException {
        int separatorIndex = details.lastIndexOf(", ");
        if (separatorIndex == -1 || details.substring(0, separatorIndex).isBlank()
                || details.substring(separatorIndex + 2).isBlank()) {
            throw new DowntownGurlException(LOAD_ERROR_MESSAGE);
        }
        return new Deadline(details.substring(0, separatorIndex),
                TaskDateTime.parseFromStorage(details.substring(separatorIndex + 2)));
    }

    /**
     * Creates an event from saved fields.
     *
     * @param parts Saved task fields.
     * @return Event represented by the saved fields.
     * @throws DowntownGurlException If the fields are not in the expected format.
     */
    private static Event createEventFromStorageParts(String[] parts) throws DowntownGurlException {
        if (parts.length == 5) {
            String description = unescapeStorageField(parts[2]);
            String from = unescapeStorageField(parts[3]);
            String to = unescapeStorageField(parts[4]);
            if (description.isBlank() || from.isBlank() || to.isBlank()) {
                throw new DowntownGurlException(LOAD_ERROR_MESSAGE);
            }
            return new Event(description, TaskDateTime.parseFromStorage(from), TaskDateTime.parseFromStorage(to));
        }
        if (parts.length != 3 || parts[2].isBlank()) {
            throw new DowntownGurlException(LOAD_ERROR_MESSAGE);
        }
        return createEventFromOldStorageDetails(parts[2]);
    }

    /**
     * Creates an event from old saved details in this form: DESCRIPTION, FROM-TO.
     *
     * @param details Old saved event details.
     * @return Event represented by the old saved details.
     * @throws DowntownGurlException If the old details are not in the expected format.
     */
    private static Event createEventFromOldStorageDetails(String details) throws DowntownGurlException {
        int detailsSeparatorIndex = details.lastIndexOf(", ");
        int timeSeparatorIndex = details.lastIndexOf("-");
        if (detailsSeparatorIndex == -1 || timeSeparatorIndex == -1 || timeSeparatorIndex <= detailsSeparatorIndex + 2) {
            throw new DowntownGurlException(LOAD_ERROR_MESSAGE);
        }
        String description = details.substring(0, detailsSeparatorIndex);
        String from = details.substring(detailsSeparatorIndex + 2, timeSeparatorIndex);
        String to = details.substring(timeSeparatorIndex + 1);
        if (description.isBlank() || from.isBlank() || to.isBlank()) {
            throw new DowntownGurlException(LOAD_ERROR_MESSAGE);
        }
        return new Event(description, TaskDateTime.parseFromStorage(from), TaskDateTime.parseFromStorage(to));
    }

    /**
     * Restores special characters from a saved field.
     *
     * @param field Saved field.
     * @return Unescaped field.
     * @throws DowntownGurlException If the saved field has an invalid escape sequence.
     */
    private static String unescapeStorageField(String field) throws DowntownGurlException {
        StringBuilder unescapedField = new StringBuilder();
        boolean isEscaping = false;
        for (int i = 0; i < field.length(); i++) {
            char character = field.charAt(i);
            if (isEscaping) {
                switch (character) {
                case '\\' -> unescapedField.append('\\');
                case 'r' -> unescapedField.append('\r');
                case 'n' -> unescapedField.append('\n');
                case '*' -> unescapedField.append('*');
                default -> throw new DowntownGurlException(LOAD_ERROR_MESSAGE);
                }
                isEscaping = false;
            } else if (character == '\\') {
                isEscaping = true;
            } else {
                unescapedField.append(character);
            }
        }
        if (isEscaping) {
            throw new DowntownGurlException(LOAD_ERROR_MESSAGE);
        }
        return unescapedField.toString();
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

        Path tempFilePath = null;
        try {
            Files.createDirectories(TASK_FILE_PATH.getParent());
            tempFilePath = Files.createTempFile(TASK_FILE_PATH.getParent(), "downtownGurl", ".tmp");
            Files.write(tempFilePath, taskLines);
            Files.move(tempFilePath, TASK_FILE_PATH, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new DowntownGurlException(SAVE_ERROR_MESSAGE);
        } finally {
            deleteTempFile(tempFilePath);
        }
    }

    /**
     * Deletes a temporary save file if one was left behind.
     *
     * @param tempFilePath Temporary file to delete.
     */
    private static void deleteTempFile(Path tempFilePath) {
        if (tempFilePath == null) {
            return;
        }
        try {
            Files.deleteIfExists(tempFilePath);
        } catch (IOException e) {
            // The main save result has already been reported, so this cleanup error can be ignored.
        }
    }

    /**
     * Prints all tasks currently in the list.
     *
     * @param tasks Current task list.
     */
    private static void printTaskList(ArrayList<Task> tasks) {
        tasks.sort(Comparator.comparing(Task::getSortDateTime, Comparator.nullsLast(Comparator.naturalOrder())));
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
