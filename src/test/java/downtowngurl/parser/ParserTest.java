package downtowngurl.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import downtowngurl.command.Command;
import downtowngurl.exception.DowntownGurlException;
import downtowngurl.storage.Storage;
import downtowngurl.task.TaskList;
import downtowngurl.task.Todo;
import downtowngurl.ui.Ui;

/**
 * Tests conversion of raw user input into executable commands.
 */
public class ParserTest {
    private final Storage storage = new NoOpStorage();
    private final Ui ui = new Ui();

    @Test
    public void parse_byeCommand_returnsExitCommand() throws DowntownGurlException {
        Command command = Parser.parse("bye");

        assertTrue(command.isExit());
    }

    @Test
    public void parse_todoCommand_addsTodoTask() throws DowntownGurlException {
        TaskList tasks = new TaskList();

        Parser.parse("todo read book").execute(tasks, this.storage, this.ui);

        assertEquals(1, tasks.size());
        assertEquals("[T][ ] read book", tasks.get(0).toString());
    }

    @Test
    public void parse_deadlineCommand_addsDeadlineTask() throws DowntownGurlException {
        TaskList tasks = new TaskList();

        Parser.parse("deadline return book /by 2/12/2019 1800").execute(tasks, this.storage, this.ui);

        assertEquals(1, tasks.size());
        assertEquals("[D][ ] return book (by: 02 Dec 2019, Monday 18:00)", tasks.get(0).toString());
    }

    @Test
    public void parse_eventCommand_addsEventTask() throws DowntownGurlException {
        TaskList tasks = new TaskList();

        Parser.parse("event project meeting /from 2/12/2019 1800 /to 2/12/2019 2000")
                .execute(tasks, this.storage, this.ui);

        assertEquals(1, tasks.size());
        assertEquals("[E][ ] project meeting (from: 02 Dec 2019, Monday 18:00 to: 02 Dec 2019, Monday 20:00)",
                tasks.get(0).toString());
    }

    @Test
    public void parse_markCommand_marksSelectedTaskAsDone() throws DowntownGurlException {
        TaskList tasks = new TaskList();
        tasks.add(new Todo("read book"));

        Parser.parse("mark 1").execute(tasks, this.storage, this.ui);

        assertTrue(tasks.get(0).isDone());
    }

    @Test
    public void parse_unmarkCommand_marksSelectedTaskAsNotDone() throws DowntownGurlException {
        TaskList tasks = new TaskList();
        Todo task = new Todo("read book");
        task.markAsDone();
        tasks.add(task);

        Parser.parse("unmark 1").execute(tasks, this.storage, this.ui);

        assertFalse(tasks.get(0).isDone());
    }

    @Test
    public void parse_deleteCommand_removesSelectedTask() throws DowntownGurlException {
        TaskList tasks = new TaskList();
        tasks.add(new Todo("read book"));
        tasks.add(new Todo("write report"));

        Parser.parse("delete 1").execute(tasks, this.storage, this.ui);

        assertEquals(1, tasks.size());
        assertEquals("[T][ ] write report", tasks.get(0).toString());
    }

    @Test
    public void parse_unknownCommand_throwsDowntownGurlException() {
        assertThrows(DowntownGurlException.class, () -> Parser.parse("dance"));
    }

    @Test
    public void parse_emptyTodoDescription_throwsDowntownGurlException() {
        assertThrows(DowntownGurlException.class, () -> Parser.parse("todo   "));
    }

    @Test
    public void parse_missingDeadlineDate_throwsDowntownGurlException() {
        assertThrows(DowntownGurlException.class, () -> Parser.parse("deadline return book"));
    }

    @Test
    public void parse_missingEventEndDate_throwsDowntownGurlException() {
        assertThrows(DowntownGurlException.class,
                () -> Parser.parse("event project meeting /from 2/12/2019 1800"));
    }

    @Test
    public void parse_invalidTaskNumber_throwsDowntownGurlException() {
        assertThrows(DowntownGurlException.class, () -> Parser.parse("mark 0"));
        assertThrows(DowntownGurlException.class, () -> Parser.parse("mark one"));
    }

    /**
     * Storage double that lets command execution mutate the task list without touching disk.
     */
    private static class NoOpStorage extends Storage {
        NoOpStorage() {
            super(Path.of("unused.txt"));
        }

        @Override
        public void saveTasks(Iterable<downtowngurl.task.Task> tasks) {
            // Tests here focus on parser output and command effects, not persistence.
        }
    }
}
