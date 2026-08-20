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
            if (command.equals("bye")) {
                System.out.println("That's bombz. Byes!");
                System.out.println(DIVIDER);
                break;
            }

            if (command.equals("list")) {
                printTaskList(tasks, taskCount);
                continue;
            }

            if (command.startsWith("mark ")) {
                Task task = getTaskByNumberFromCommand(tasks, taskCount, command, 5);
                if (task == null) {
                    printErrorMessage(UNKNOWN_COMMAND_MESSAGE);
                    continue;
                }
                task.markAsDone();
                printUpdatedTaskMessage("Kays, I've marked this task as done!", task);
                continue;
            }

            if (command.startsWith("unmark ")) {
                Task task = getTaskByNumberFromCommand(tasks, taskCount, command, 7);
                if (task == null) {
                    printErrorMessage(UNKNOWN_COMMAND_MESSAGE);
                    continue;
                }
                task.markAsNotDone();
                printUpdatedTaskMessage("Sure, I unmarked it!", task);
                continue;
            }

            if (command.equals("todo") || command.startsWith("todo ")) {
                if (!hasTodoDescription(command)) {
                    printErrorMessage(EMPTY_TASK_MESSAGE);
                    continue;
                }
                tasks[taskCount] = new Todo(command.substring(5));
                taskCount = printAddedTaskMessage(tasks, taskCount);
                continue;
            }

            if (command.equals("deadline") || command.startsWith("deadline ")) {
                if (!hasDeadlineDetails(command)) {
                    printErrorMessage(EMPTY_TASK_MESSAGE);
                    continue;
                }
                tasks[taskCount] = createDeadline(command);
                taskCount = printAddedTaskMessage(tasks, taskCount);
                continue;
            }

            if (command.equals("event") || command.startsWith("event ")) {
                if (!hasEventDetails(command)) {
                    printErrorMessage(EMPTY_TASK_MESSAGE);
                    continue;
                }
                tasks[taskCount] = createEvent(command);
                taskCount = printAddedTaskMessage(tasks, taskCount);
                continue;
            }

            printErrorMessage(UNKNOWN_COMMAND_MESSAGE);
        }
    }

    /**
     * Checks whether a todo command includes a non-empty description.
     *
     * @param command Full user command.
     * @return True if the todo description is present.
     */
    private static boolean hasTodoDescription(String command) {
        return command.length() > 5 && !command.substring(5).isBlank();
    }

    /**
     * Checks whether a deadline command includes both a description and a due date/time.
     *
     * @param command Full user command.
     * @return True if all deadline fields are present.
     */
    private static boolean hasDeadlineDetails(String command) {
        int separatorIndex = command.indexOf(DEADLINE_SEPARATOR);
        if (separatorIndex == -1) {
            return false;
        }
        String description = command.substring(9, separatorIndex);
        String by = command.substring(separatorIndex + DEADLINE_SEPARATOR.length());
        return !description.isBlank() && !by.isBlank();
    }

    /**
     * Checks whether an event command includes a description, start, and end.
     *
     * @param command Full user command.
     * @return True if all event fields are present.
     */
    private static boolean hasEventDetails(String command) {
        int fromIndex = command.indexOf(EVENT_FROM_SEPARATOR);
        int toIndex = command.indexOf(EVENT_TO_SEPARATOR);
        if (fromIndex == -1 || toIndex == -1 || fromIndex >= toIndex) {
            return false;
        }
        String description = command.substring(6, fromIndex);
        String from = command.substring(fromIndex + EVENT_FROM_SEPARATOR.length(), toIndex);
        String to = command.substring(toIndex + EVENT_TO_SEPARATOR.length());
        return !description.isBlank() && !from.isBlank() && !to.isBlank();
    }

    /**
     * Creates a deadline task from a command in this form: deadline DESCRIPTION /by TIME.
     *
     * @param command Full user command.
     * @return New deadline task.
     */
    private static Deadline createDeadline(String command) {
        int separatorIndex = command.indexOf(DEADLINE_SEPARATOR);
        String description = command.substring(9, separatorIndex);
        String by = command.substring(separatorIndex + DEADLINE_SEPARATOR.length());
        return new Deadline(description, by);
    }

    /**
     * Creates an event task from a command in this form: event DESCRIPTION /from START /to END.
     *
     * @param command Full user command.
     * @return New event task.
     */
    private static Event createEvent(String command) {
        int fromIndex = command.indexOf(EVENT_FROM_SEPARATOR);
        int toIndex = command.indexOf(EVENT_TO_SEPARATOR);
        String description = command.substring(6, fromIndex);
        String from = command.substring(fromIndex + EVENT_FROM_SEPARATOR.length(), toIndex);
        String to = command.substring(toIndex + EVENT_TO_SEPARATOR.length());
        return new Event(description, from, to);
    }

    /**
     * Finds the task number in a mark or unmark command and returns the matching task.
     *
     * @param tasks Current task list.
     * @param taskCount Number of tasks in the list.
     * @param command Full user command.
     * @param taskNumberStartIndex Index where the task number starts.
     * @return Task selected by the user, or null if the task number is invalid.
     */
    private static Task getTaskByNumberFromCommand(Task[] tasks, int taskCount, String command, int taskNumberStartIndex) {
        int taskNumber;
        try {
            taskNumber = Integer.parseInt(command.substring(taskNumberStartIndex));
        } catch (NumberFormatException e) {
            return null;
        }
        if (taskNumber < 1 || taskNumber > taskCount) {
            return null;
        }
        return tasks[taskNumber - 1];
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
     * Prints an error message using the same divider style as normal chatbot replies.
     *
     * @param message Error message to show.
     */
    private static void printErrorMessage(String message) {
        System.out.println(message);
        System.out.println(DIVIDER);
    }
}
