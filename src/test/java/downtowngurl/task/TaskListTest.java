package downtowngurl.task;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

/**
 * Tests task list operations that contain ordering logic.
 */
public class TaskListTest {
    @Test
    public void sortByDate_mixedDatedAndUndatedTasks_ordersDatedTasksBeforeTodos() {
        TaskList tasks = new TaskList();
        Todo todo = new Todo("read book");
        Deadline laterDeadline = new Deadline("submit report", LocalDateTime.of(2019, 12, 3, 18, 0));
        Event earlierEvent = new Event("meeting", LocalDateTime.of(2019, 12, 2, 18, 0),
                LocalDateTime.of(2019, 12, 2, 20, 0));
        tasks.add(todo);
        tasks.add(laterDeadline);
        tasks.add(earlierEvent);

        tasks.sortByDate();

        assertEquals(earlierEvent, tasks.get(0));
        assertEquals(laterDeadline, tasks.get(1));
        assertEquals(todo, tasks.get(2));
    }
}
