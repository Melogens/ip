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
    private TaskList tasks;

    /**
     * Creates the chatbot application using the given file for saved tasks.
     *
     * @param taskFilePath Path to the file used to persist tasks.
     */
    public DowntownGurl(Path taskFilePath) {
        this.ui = new Ui();
        this.storage = new Storage(taskFilePath);
        this.tasks = new TaskList();
    }

    /**
     * Starts reading and handling user commands.
     */
    public void run() {
        this.ui.showWelcome();
        this.tasks = loadTasks();
        boolean isExit = false;
        while (!isExit && this.ui.hasNextCommand()) {
            try {
                String fullCommand = this.ui.readCommand();
                this.ui.showLine();
                Command command = Parser.parse(fullCommand);
                command.execute(this.tasks, this.storage, this.ui);
                isExit = command.isExit();
            } catch (DowntownGurlException e) {
                this.ui.showError(e.getMessage());
            } finally {
                this.ui.showLine();
            }
        }
    }

    public static void main(String[] args) {
        new DowntownGurl(TASK_FILE_PATH).run();
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
