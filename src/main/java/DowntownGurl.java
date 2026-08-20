import java.util.Scanner;

/**
 * Entry point for the Downtown Gurl chatbot application.
 */
public class DowntownGurl {
    private static final String CHATBOT_NAME = "Downtown Gurl";
    private static final String DIVIDER = "<*><*><*><*><*><*><*><*><*><*><*><*><*><*><*><*><*><*><*><*><*><*><*><*>";
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
        System.out.println("I'm the one to give you a reality check " +
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
                System.out.println("Here's your tasks:");
                for (int i = 0; i < taskCount; i++) {
                    System.out.println(" " + (i + 1) + ". " + tasks[i]);
                }
                System.out.println(DIVIDER);
                continue;
            }

            if (command.startsWith("mark ")) {
                int taskNumber = Integer.parseInt(command.substring(5));
                int taskIndex = taskNumber - 1;
                tasks[taskIndex].markAsDone();
                System.out.println("Kays, I've marked this task as done!");
                System.out.println("  " + tasks[taskIndex]);
                System.out.println(DIVIDER);
                continue;
            }

            if (command.startsWith("unmark ")) {
                int taskNumber = Integer.parseInt(command.substring(7));
                int taskIndex = taskNumber - 1;
                tasks[taskIndex].markAsNotDone();
                System.out.println("Sure, I unmarked it!");
                System.out.println("  " + tasks[taskIndex]);
                System.out.println(DIVIDER);
                continue;
            }

            tasks[taskCount] = new Task(command);
            taskCount++;
            System.out.println("added: " + command);
            System.out.println(DIVIDER);
        }
    }
}
