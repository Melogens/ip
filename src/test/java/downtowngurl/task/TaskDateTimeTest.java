package downtowngurl.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import downtowngurl.exception.DowntownGurlException;

/**
 * Tests date-time parsing and formatting used by dated tasks.
 */
public class TaskDateTimeTest {
    @Test
    public void parse_validDateAndTime_returnsExpectedDateTime() throws DowntownGurlException {
        LocalDateTime parsedDateTime = TaskDateTime.parse("2/12/2019 1800");

        assertEquals(LocalDateTime.of(2019, 12, 2, 18, 0), parsedDateTime);
    }

    @Test
    public void parse_validDateOnly_returnsDateAtMidnight() throws DowntownGurlException {
        LocalDateTime parsedDateTime = TaskDateTime.parse("2/12/2019");

        assertEquals(LocalDateTime.of(2019, 12, 2, 0, 0), parsedDateTime);
    }

    @Test
    public void parse_validInputWithLeadingAndTrailingSpaces_returnsExpectedDateTime() throws DowntownGurlException {
        LocalDateTime parsedDateTime = TaskDateTime.parse("  2/12/2019 1800  ");

        assertEquals(LocalDateTime.of(2019, 12, 2, 18, 0), parsedDateTime);
    }

    @Test
    public void parse_invalidInput_throwsDowntownGurlException() {
        assertThrows(DowntownGurlException.class, () -> TaskDateTime.parse("2 Dec 2019 6pm"));
    }

    @Test
    public void parseFromStorage_isoDateTime_returnsExpectedDateTime() throws DowntownGurlException {
        LocalDateTime parsedDateTime = TaskDateTime.parseFromStorage("2019-12-02T18:00:00");

        assertEquals(LocalDateTime.of(2019, 12, 2, 18, 0), parsedDateTime);
    }

    @Test
    public void parseFromStorage_legacyDateAndTime_returnsExpectedDateTime() throws DowntownGurlException {
        LocalDateTime parsedDateTime = TaskDateTime.parseFromStorage("2/12/2019 1800");

        assertEquals(LocalDateTime.of(2019, 12, 2, 18, 0), parsedDateTime);
    }

    @Test
    public void parseFromStorage_legacyDateOnly_returnsDateAtMidnight() throws DowntownGurlException {
        LocalDateTime parsedDateTime = TaskDateTime.parseFromStorage("2/12/2019");

        assertEquals(LocalDateTime.of(2019, 12, 2, 0, 0), parsedDateTime);
    }

    @Test
    public void parseFromStorage_inputWithLeadingAndTrailingSpaces_returnsExpectedDateTime()
            throws DowntownGurlException {
        LocalDateTime parsedDateTime = TaskDateTime.parseFromStorage("  2019-12-02T18:00:00  ");

        assertEquals(LocalDateTime.of(2019, 12, 2, 18, 0), parsedDateTime);
    }

    @Test
    public void parseFromStorage_invalidInput_throwsDowntownGurlException() {
        assertThrows(DowntownGurlException.class, () -> TaskDateTime.parseFromStorage("2 Dec 2019 6pm"));
    }

    @Test
    public void formatForStorage_dateTime_returnsIsoDateTime() {
        LocalDateTime dateTime = LocalDateTime.of(2019, 12, 2, 18, 0);

        assertEquals("2019-12-02T18:00:00", TaskDateTime.formatForStorage(dateTime));
    }

    @Test
    public void formatForDisplay_dateTime_returnsUserFriendlyDateTime() {
        LocalDateTime dateTime = LocalDateTime.of(2019, 12, 2, 18, 0);

        assertEquals("02 Dec 2019, Monday 18:00", TaskDateTime.formatForDisplay(dateTime));
    }
}
