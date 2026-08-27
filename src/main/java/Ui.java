import java.util.Scanner;

/**
 * Handles text interactions with the user.
 */
public class Ui {
    private static final String CHATBOT_NAME = "Downtown Gurl";
    private static final String DIVIDER = "<*><*><*><*><*><*><*><*><*><*><*><*><*><*><*><*><*><*><*><*><*><*><*><*>";

    private final Scanner scanner;

    /**
     * Creates a UI helper that reads commands from standard input.
     */
    public Ui() {
        this.scanner = new Scanner(System.in);
    }

    /**
     * Prints the greeting shown when the chatbot starts.
     */
    public void showWelcome() {
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
        System.out.println("I'm here to give you a reality check "
                + "and help you manifest that life you've been dreaming.");
        System.out.println(DIVIDER);
        System.out.println("Darling what's up?");
    }

    /**
     * Returns whether the user has another command ready.
     *
     * @return true if another command can be read, false otherwise.
     */
    public boolean hasNextCommand() {
        return this.scanner.hasNextLine();
    }

    /**
     * Reads the next command from the user.
     *
     * @return Full command entered by the user.
     */
    public String readCommand() {
        return this.scanner.nextLine();
    }

    /**
     * Prints the goodbye message shown when the chatbot exits.
     */
    public void showGoodbye() {
        System.out.println("That's bombz. Byes!");
    }

    /**
     * Prints all tasks currently in the list.
     *
     * @param tasks Current task list.
     */
    public void showTaskList(TaskList tasks) {
        tasks.sortByDate();
        System.out.println("Here's your tasks:");
        for (int i = 0; i < tasks.size(); i++) {
            System.out.println(" " + (i + 1) + ". " + tasks.get(i));
        }
    }

    /**
     * Prints the result of marking or unmarking a task.
     *
     * @param message Confirmation message.
     * @param task Updated task.
     */
    public void showUpdatedTask(String message, Task task) {
        System.out.println(message);
        System.out.println("  " + task);
    }

    /**
     * Prints a confirmation for the newly added task.
     *
     * @param addedTask Task that was added.
     * @param taskCount Number of tasks after adding the task.
     */
    public void showAddedTask(Task addedTask, int taskCount) {
        System.out.println("Gotcha. Noted it downz:");
        System.out.println("  " + addedTask);
        System.out.println("Now you got " + taskCount + " tasks in the roster.");
    }

    /**
     * Prints a confirmation for the deleted task.
     *
     * @param removedTask Task that was removed.
     * @param taskCount Number of tasks after removing the task.
     */
    public void showDeletedTask(Task removedTask, int taskCount) {
        System.out.println("Sure~ I've removed this task:");
        System.out.println("  " + removedTask);
        System.out.println("Now you got " + taskCount + " tasks in the list.");
    }

    /**
     * Prints an error message using the same divider style as normal chatbot replies.
     *
     * @param message Error message to show.
     */
    public void showError(String message) {
        System.out.println(message);
    }

    /**
     * Prints a divider line between chatbot interactions.
     */
    public void showLine() {
        System.out.println(DIVIDER);
    }
}
