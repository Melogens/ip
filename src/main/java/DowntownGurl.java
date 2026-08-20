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
    private static final String EMPTY_TASK_MESSAGE = "Soz queen you gotta at least give me SOMETHING to work with.";
    private static final String UNKNOWN_COMMAND_MESSAGE = "U sleeping alright? Sounds like you ain't...";
    private static final int EXIT_TASK_COUNT = -1;

    public static void main(String[] args) {
        String banner = """
                 ____                      _                       ____           __\s
                |  _ \\  _____      ___ __ | |_ _____      ___ __  / ___|_   _ _ __| |
                | | | |/ _ \\ \\ /\\ / / '_ \\| __/ _ \\ \\ /\\ / / '_ \\| |___| | | '__| | |
                | |_| | (_) \\ V  V /| | | | || (_) \\ V  V /| | | | |_| | |_| | |  | |
                |____/ \\___/ \\_/\\_/ |_| |_|\\__\\___/ \\_/\\_/ |_| |_|\\____|\\__,_|_|  |_|""";
        System.out.println(DIVIDER);
        System.out.println(banner);
        System.out.println(DIVIDER);
        System.out.println("Hey I'm " + CHATBOT_NAME + ".");
        System.out.println("I'm here to give you a reality check " +
                "and help you manifest that life you've been dreaming.");
        System.out.println("Darling what's up?");
        System.out.println(DIVIDER);

        Task[] tasks = new Task[100];
        int taskCount = 0;

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String command = scanner.nextLine();
            try {
                taskCount = handleCommand(command, tasks, taskCount);
                if (taskCount == EXIT_TASK_COUNT) {
                    break;
                }
            } catch (DowntownGurlException e) {
                printErrorMessage(e.getMessage());
            }
        }
    }

    /**
     * Handles one user command and returns the updated task count.
     *
     * @param command Full user command.
     * @param tasks Current task list.
     * @param taskCount Number of tasks in the list before handling the command.
     * @return Updated task count, or EXIT_TASK_COUNT if the user wants to exit.
     * @throws DowntownGurlException If the command cannot be handled.
     */
    private static int handleCommand(String command, Task[] tasks, int taskCount) throws DowntownGurlException {
        if (command.equals("bye")) {
            System.out.println("That's bombz. Byes!");
            System.out.println(DIVIDER);
            return EXIT_TASK_COUNT;
        }

        if (command.equals("list")) {
            printTaskList(tasks, taskCount);
            return taskCount;
        }

        if (command.startsWith("mark ")) {
            Task task = getTaskByNumberFromCommand(tasks, taskCount, command, 5);
            task.markAsDone();
            printUpdatedTaskMessage("Kays, I've marked this task as done!", task);
            return taskCount;
        }

        if (command.startsWith("unmark ")) {
            Task task = getTaskByNumberFromCommand(tasks, taskCount, command, 7);
            task.markAsNotDone();
            printUpdatedTaskMessage("Sure, I unmarked it!", task);
            return taskCount;
        }

        if (command.startsWith("delete ")) {
            int taskIndex = getTaskIndexFromCommand(taskCount, command, 7);
            Task removedTask = tasks[taskIndex];
            removeTaskAtIndex(tasks, taskCount, taskIndex);
            return printDeletedTaskMessage(removedTask, taskCount);
        }

        if (command.equals("todo") || command.startsWith("todo ")) {
            tasks[taskCount] = createTodo(command);
            return printAddedTaskMessage(tasks, taskCount);
        }

        if (command.equals("deadline") || command.startsWith("deadline ")) {
            tasks[taskCount] = createDeadline(command);
            return printAddedTaskMessage(tasks, taskCount);
        }

        if (command.equals("event") || command.startsWith("event ")) {
            tasks[taskCount] = createEvent(command);
            return printAddedTaskMessage(tasks, taskCount);
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
     * Finds the task number in a mark or unmark command and returns the matching task.
     *
     * @param tasks Current task list.
     * @param taskCount Number of tasks in the list.
     * @param command Full user command.
     * @param taskNumberStartIndex Index where the task number starts.
     * @return Task selected by the user.
     * @throws DowntownGurlException If the task number is not valid.
     */
    private static Task getTaskByNumberFromCommand(Task[] tasks, int taskCount, String command, int taskNumberStartIndex)
            throws DowntownGurlException {
        return tasks[getTaskIndexFromCommand(taskCount, command, taskNumberStartIndex)];
    }

    /**
     * Finds the zero-based task index in a command containing a one-based task number.
     *
     * @param taskCount Number of tasks in the list.
     * @param command Full user command.
     * @param taskNumberStartIndex Index where the task number starts.
     * @return Zero-based index of the task selected by the user.
     * @throws DowntownGurlException If the task number is not valid.
     */
    private static int getTaskIndexFromCommand(int taskCount, String command, int taskNumberStartIndex)
            throws DowntownGurlException {
        int taskNumber;
        try {
            taskNumber = Integer.parseInt(command.substring(taskNumberStartIndex).trim());
        } catch (NumberFormatException e) {
            throw new DowntownGurlException(UNKNOWN_COMMAND_MESSAGE);
        }
        if (taskNumber < 1 || taskNumber > taskCount) {
            throw new DowntownGurlException(UNKNOWN_COMMAND_MESSAGE);
        }
        return taskNumber - 1;
    }

    /**
     * Removes one task and shifts later tasks forward to keep the array compact.
     *
     * @param tasks Current task list.
     * @param taskCount Number of tasks in the list before removal.
     * @param taskIndex Zero-based index of the task to remove.
     */
    private static void removeTaskAtIndex(Task[] tasks, int taskCount, int taskIndex) {
        for (int i = taskIndex; i < taskCount - 1; i++) {
            tasks[i] = tasks[i + 1];
        }
        tasks[taskCount - 1] = null;
    }

    /**
     * Prints all tasks currently in the list.
     *
     * @param tasks Current task list.
     * @param taskCount Number of tasks in the list.
     */
    private static void printTaskList(Task[] tasks, int taskCount) {
        System.out.println("Here's your tasks:");
        for (int i = 0; i < taskCount; i++) {
            System.out.println(" " + (i + 1) + ". " + tasks[i]);
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
     * @param tasks Current task list.
     * @param taskCount Number of tasks before the new task is counted.
     * @return Updated task count.
     */
    private static int printAddedTaskMessage(Task[] tasks, int taskCount) {
        int updatedTaskCount = taskCount + 1;
        System.out.println("Gotcha. Noted it downz:");
        System.out.println("  " + tasks[taskCount]);
        System.out.println("Now you got " + updatedTaskCount + " tasks in the roster.");
        System.out.println(DIVIDER);
        return updatedTaskCount;
    }

    /**
     * Prints a confirmation for the deleted task.
     *
     * @param removedTask Task that was removed.
     * @param taskCount Number of tasks before removal.
     * @return Updated task count.
     */
    private static int printDeletedTaskMessage(Task removedTask, int taskCount) {
        int updatedTaskCount = taskCount - 1;
        System.out.println("Sure~ I've removed this task:");
        System.out.println("  " + removedTask);
        System.out.println("Now you got " + updatedTaskCount + " tasks in the list.");
        System.out.println(DIVIDER);
        return updatedTaskCount;
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
