package downtowngurl.parser;

import downtowngurl.command.AddCommand;
import downtowngurl.command.ByeCommand;
import downtowngurl.command.Command;
import downtowngurl.command.DeleteCommand;
import downtowngurl.command.FindCommand;
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
    private static final String ARGUMENT_SEPARATOR = " ";
    private static final String BYE_COMMAND = "bye";
    private static final String LIST_COMMAND = "list";
    private static final String FIND_COMMAND = "find";
    private static final String MARK_COMMAND = "mark";
    private static final String UNMARK_COMMAND = "unmark";
    private static final String DELETE_COMMAND = "delete";
    private static final String TODO_COMMAND = "todo";
    private static final String DEADLINE_COMMAND = "deadline";
    private static final String EVENT_COMMAND = "event";
    private static final String DEADLINE_SEPARATOR = " /by ";
    private static final String EVENT_FROM_SEPARATOR = " /from ";
    private static final String EVENT_TO_SEPARATOR = " /to ";
    private static final String EMPTY_TASK_MESSAGE = "Soz queen you gotta at least give me SOMETHING to work with.";
    private static final String DEADLINE_FORMAT_HINT = EMPTY_TASK_MESSAGE
            + "\nMaybe you could try formatting it as: deadline <task name> /by dd/mm/yyyy time";
    private static final String EVENT_FORMAT_HINT = EMPTY_TASK_MESSAGE
            + "\nMaybe you could try formatting it as: event <event name> /from dd/mm/yyyy time /to dd/mm/yyyy time";
    private static final String EMPTY_FIND_KEYWORD_MESSAGE = "Soz queen you gotta give me a keyword to find.";
    private static final String UNKNOWN_COMMAND_MESSAGE = "U sleeping alright? Sounds like you ain't...";

    /**
     * Parses a full user command into a command object the application can execute.
     *
     * @param command Full user command.
     * @return Parsed command.
     * @throws DowntownGurlException If the command is invalid.
     */
    public static Command parse(String command) throws DowntownGurlException {
        if (command.equals(BYE_COMMAND)) {
            return new ByeCommand();
        }

        if (command.equals(LIST_COMMAND)) {
            return new ListCommand();
        }

        if (isCommandOrArgumentStart(command, FIND_COMMAND)) {
            return new FindCommand(getFindKeyword(command));
        }

        if (startsWithCommandArgument(command, MARK_COMMAND)) {
            return new MarkCommand(getTaskIndexFromCommand(command, getArgumentStartIndex(MARK_COMMAND)));
        }

        if (startsWithCommandArgument(command, UNMARK_COMMAND)) {
            return new UnmarkCommand(getTaskIndexFromCommand(command, getArgumentStartIndex(UNMARK_COMMAND)));
        }

        if (startsWithCommandArgument(command, DELETE_COMMAND)) {
            return new DeleteCommand(getTaskIndexFromCommand(command, getArgumentStartIndex(DELETE_COMMAND)));
        }

        if (isCommandOrArgumentStart(command, TODO_COMMAND)) {
            return new AddCommand(createTodo(command));
        }

        if (isCommandOrArgumentStart(command, DEADLINE_COMMAND)) {
            return new AddCommand(createDeadline(command));
        }

        if (isCommandOrArgumentStart(command, EVENT_COMMAND)) {
            return new AddCommand(createEvent(command));
        }

        throw new DowntownGurlException(UNKNOWN_COMMAND_MESSAGE);
    }

    /**
     * Extracts the keyword from a find command.
     *
     * @param command Full user command.
     * @return Keyword to search for.
     * @throws DowntownGurlException If the keyword is missing.
     */
    private static String getFindKeyword(String command) throws DowntownGurlException {
        String keyword = getCommandArgument(command, FIND_COMMAND);
        if (keyword.isBlank()) {
            throw new DowntownGurlException(EMPTY_FIND_KEYWORD_MESSAGE);
        }
        return keyword.trim();
    }

    /**
     * Creates a todo task from a command in this form: todo DESCRIPTION.
     *
     * @param command Full user command.
     * @return New todo task.
     * @throws DowntownGurlException If the todo description is missing.
     */
    private static Todo createTodo(String command) throws DowntownGurlException {
        String description = getCommandArgument(command, TODO_COMMAND);
        if (description.isBlank()) {
            throw new DowntownGurlException(EMPTY_TASK_MESSAGE);
        }
        return new Todo(description);
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
        String description = command.substring(getArgumentStartIndex(DEADLINE_COMMAND), separatorIndex);
        assert separatorIndex >= 9 : "Deadline separator should appear after the command word.";
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
        String description = command.substring(getArgumentStartIndex(EVENT_COMMAND), fromIndex);
        assert fromIndex >= 6 : "Event start separator should appear after the command word.";
        assert toIndex > fromIndex : "Event end separator should appear after the start separator.";
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

    /**
     * Returns whether the command is exactly the command word or begins with its argument separator.
     *
     * @param command Full user command.
     * @param commandWord Command word to match.
     * @return true if the input matches the command word or its argument form.
     */
    private static boolean isCommandOrArgumentStart(String command, String commandWord) {
        return command.equals(commandWord) || startsWithCommandArgument(command, commandWord);
    }

    /**
     * Returns whether the command begins with the command word followed by an argument separator.
     *
     * @param command Full user command.
     * @param commandWord Command word to match.
     * @return true if the input starts with the command word and an argument separator.
     */
    private static boolean startsWithCommandArgument(String command, String commandWord) {
        return command.startsWith(commandWord + ARGUMENT_SEPARATOR);
    }

    /**
     * Extracts the argument portion after the command word and its separator.
     *
     * @param command Full user command.
     * @param commandWord Command word at the start of the input.
     * @return Argument text, or an empty string if there is no argument separator.
     */
    private static String getCommandArgument(String command, String commandWord) {
        if (command.equals(commandWord)) {
            return "";
        }
        assert startsWithCommandArgument(command, commandWord) : "Command should start with the expected word.";
        return command.substring(getArgumentStartIndex(commandWord));
    }

    /**
     * Returns the index where a command's argument starts.
     *
     * @param commandWord Command word before the argument.
     * @return Zero-based argument start index.
     */
    private static int getArgumentStartIndex(String commandWord) {
        return commandWord.length() + ARGUMENT_SEPARATOR.length();
    }
}
