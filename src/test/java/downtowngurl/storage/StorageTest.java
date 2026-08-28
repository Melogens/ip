package downtowngurl.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import downtowngurl.exception.DowntownGurlException;
import downtowngurl.task.Deadline;
import downtowngurl.task.Event;
import downtowngurl.task.Task;
import downtowngurl.task.TaskList;
import downtowngurl.task.Todo;

/**
 * Tests loading and saving tasks through the storage file format.
 */
public class StorageTest {
    @TempDir
    public Path tempDir;

    @Test
    public void loadTasks_missingTaskFile_returnsEmptyList() throws DowntownGurlException {
        Storage storage = new Storage(this.tempDir.resolve("data").resolve("tasks.txt"));

        ArrayList<Task> tasks = storage.loadTasks();

        assertTrue(tasks.isEmpty());
        assertTrue(storage.getCorruptedLineNumbers().isEmpty());
    }

    @Test
    public void loadTasks_validModernStorageLines_returnsExpectedTasks()
            throws DowntownGurlException, IOException {
        Path taskFilePath = createTaskFile(
                "T * Not done * read book",
                "D * Done * submit report * 2019-12-02T18:00:00",
                "E * Not done * meeting * 2019-12-02T18:00:00 * 2019-12-02T20:00:00");
        Storage storage = new Storage(taskFilePath);

        ArrayList<Task> tasks = storage.loadTasks();

        assertEquals(3, tasks.size());
        assertEquals("[T][ ] read book", tasks.get(0).toString());
        assertEquals("[D][X] submit report (by: 02 Dec 2019, Monday 18:00)", tasks.get(1).toString());
        assertEquals("[E][ ] meeting (from: 02 Dec 2019, Monday 18:00 to: 02 Dec 2019, Monday 20:00)",
                tasks.get(2).toString());
        assertTrue(storage.getCorruptedLineNumbers().isEmpty());
    }

    @Test
    public void loadTasks_escapedStorageField_returnsUnescapedDescription()
            throws DowntownGurlException, IOException {
        Path taskFilePath = createTaskFile("T * Not done * line one\\nline two \\* urgent \\\\ done");
        Storage storage = new Storage(taskFilePath);

        ArrayList<Task> tasks = storage.loadTasks();

        assertEquals(1, tasks.size());
        assertEquals("[T][ ] line one\nline two * urgent \\ done", tasks.get(0).toString());
    }

    @Test
    public void loadTasks_corruptedLines_skipsInvalidLinesAndRecordsLineNumbers()
            throws DowntownGurlException, IOException {
        Path taskFilePath = createTaskFile(
                "T * Not done * read book",
                "bad saved line",
                "D * Later * submit report * 2019-12-02T18:00:00",
                "",
                "T * Done * write report");
        Storage storage = new Storage(taskFilePath);

        ArrayList<Task> tasks = storage.loadTasks();

        assertEquals(2, tasks.size());
        assertEquals("[T][ ] read book", tasks.get(0).toString());
        assertEquals("[T][X] write report", tasks.get(1).toString());
        assertEquals(List.of(2, 3), storage.getCorruptedLineNumbers());
    }

    @Test
    public void saveTasks_taskList_writesModernStorageLines() throws DowntownGurlException, IOException {
        Path taskFilePath = this.tempDir.resolve("data").resolve("tasks.txt");
        Storage storage = new Storage(taskFilePath);
        TaskList tasks = new TaskList();
        Todo todo = new Todo("line one\nline two * urgent \\ done");
        Deadline deadline = new Deadline("submit report", LocalDateTime.of(2019, 12, 2, 18, 0));
        deadline.markAsDone();
        Event event = new Event("meeting", LocalDateTime.of(2019, 12, 2, 18, 0),
                LocalDateTime.of(2019, 12, 2, 20, 0));
        tasks.add(todo);
        tasks.add(deadline);
        tasks.add(event);

        storage.saveTasks(tasks);

        assertEquals(List.of(
                "T * Not done * line one\\nline two \\* urgent \\\\ done",
                "D * Done * submit report * 2019-12-02T18:00:00",
                "E * Not done * meeting * 2019-12-02T18:00:00 * 2019-12-02T20:00:00"),
                Files.readAllLines(taskFilePath));
    }

    private Path createTaskFile(String... lines) throws IOException {
        Path taskFilePath = this.tempDir.resolve("tasks.txt");
        Files.write(taskFilePath, List.of(lines));
        return taskFilePath;
    }
}
