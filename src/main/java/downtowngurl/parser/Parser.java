package downtowngurl.parser;

import downtowngurl.command.AddCommand;
import downtowngurl.command.ByeCommand;
import downtowngurl.command.Command;
import downtowngurl.command.DeleteCommand;
import downtowngurl.command.ListCommand;
import downtowngurl.command.MarkCommand;
import downtowngurl.command.UnmarkCommand;
import downtowngurl.exception.DowntownGurlException;
import downtowngurl.task.Deadline;
import downtowngurl.task.Event;
import downtowngurl.task.TaskDateTime;
import downtowngurl.task.Todo;

/**
 * Makes sense of raw user commands.
 */
public class Parser {
    private static final String DEADLINE_SEPARATOR = " /by ";
    private static final String EVENT_FROM_SEPARATOR = " /from ";
    private static final String EVENT_TO_SEPARATOR = " /to ";
    private static final String EMPTY_TASK_MESSAGE = "Soz queen you gotta at least give me SOMETHING to work with.";
    private static final String DEADLINE_FORMAT_HINT = EMPTY_TASK_MESSAGE
            + "\nMaybe you could try formatting it as: deadline <task name> /by dd/mm/yyyy time";
    private static final String EVENT_FORMAT_HINT = EMPTY_TASK_MESSAGE
            + "\nMaybe you could try formatting it as: event <event name> /from dd/mm/yyyy time /to dd/mm/yyyy time";
    private static final String UNKNOWN_COMMAND_MESSAGE = "U sleeping alright? Sounds like you ain't...";

    /**
     * Parses a full user command into a command object the application can execute.
     *
     * @param command Full user command.
     * @return Parsed command.
     * @throws DowntownGurlException If the command is invalid.
     */
    public static Command parse(String command) throws DowntownGurlException {
        if (command.equals("bye")) {
            return new ByeCommand();
        }

        if (command.equals("list")) {
            return new ListCommand();
        }

        if (command.startsWith("mark ")) {
            return new MarkCommand(getTaskIndexFromCommand(command, 5));
        }

        if (command.startsWith("unmark ")) {
            return new UnmarkCommand(getTaskIndexFromCommand(command, 7));
        }

        if (command.startsWith("delete ")) {
            return new DeleteCommand(getTaskIndexFromCommand(command, 7));
        }

        if (command.equals("todo") || command.startsWith("todo ")) {
            return new AddCommand(createTodo(command));
        }

        if (command.equals("deadline") || command.startsWith("deadline ")) {
            return new AddCommand(createDeadline(command));
        }

        if (command.equals("event") || command.startsWith("event ")) {
            return new AddCommand(createEvent(command));
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
     * Finds the zero-based task index in a command containing a one-based task number.
     *
     * @param command Full user command.
     * @param taskNumberStartIndex Index where the task number starts.
     * @return Zero-based index of the task selected by the user.
     * @throws DowntownGurlException If the task number is not valid.
     */
    private static int getTaskIndexFromCommand(String command, int taskNumberStartIndex)
            throws DowntownGurlException {
        int taskNumber;
        try {
            taskNumber = Integer.parseInt(command.substring(taskNumberStartIndex).trim());
        } catch (NumberFormatException e) {
            throw new DowntownGurlException(UNKNOWN_COMMAND_MESSAGE);
        }
        if (taskNumber < 1) {
            throw new DowntownGurlException(UNKNOWN_COMMAND_MESSAGE);
        }
        return taskNumber - 1;
    }
}
