package jvm.exercises.exercise3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class LogLineParserTest {
    private final LogLineParser parser = new LogLineParser();

    @Test
    void parsesTimestampLevelLoggerAndMessage() {
        LogEvent event = parser.parse("2026-10-10T13:45:12Z INFO [CheckoutService] Payment accepted");

        assertEquals(Instant.parse("2026-10-10T13:45:12Z"), event.timestamp());
        assertEquals(Level.INFO, event.level());
        assertEquals("CheckoutService", event.logger());
        assertEquals(Optional.empty(), event.correlationId());
        assertEquals("Payment accepted", event.message());
    }

    @Test
    void parsesFractionalSecondTimestamp() {
        LogEvent event = parser.parse("2026-10-10T13:45:12.123456Z WARN [Auth] Token expiring soon");

        assertEquals(Instant.parse("2026-10-10T13:45:12.123456Z"), event.timestamp());
        assertEquals(Level.WARN, event.level());
    }

    @Test
    void parsesOptionalCorrelationId() {
        LogEvent event = parser.parse("2026-10-10T13:45:12Z INFO [CheckoutService] (req-42) Payment accepted");

        assertEquals(Optional.of("req-42"), event.correlationId());
        assertEquals("Payment accepted", event.message());
    }

    @Test
    void parsesEveryLevel() {
        assertEquals(Level.TRACE, parser.parse("2026-10-10T13:45:12Z TRACE [x] t").level());
        assertEquals(Level.DEBUG, parser.parse("2026-10-10T13:45:12Z DEBUG [x] d").level());
        assertEquals(Level.INFO, parser.parse("2026-10-10T13:45:12Z INFO [x] i").level());
        assertEquals(Level.WARN, parser.parse("2026-10-10T13:45:12Z WARN [x] w").level());
        assertEquals(Level.ERROR, parser.parse("2026-10-10T13:45:12Z ERROR [x] e").level());
    }

    @Test
    void parsesLevelCaseInsensitively() {
        assertEquals(Level.WARN, parser.parse("2026-10-10T13:45:12Z warn [x] hi").level());
    }

    @Test
    void toleratesExtraWhitespaceBetweenFields() {
        LogEvent event = parser.parse("   2026-10-10T13:45:12Z    INFO   [CheckoutService]    Payment   accepted   ");

        assertEquals(Instant.parse("2026-10-10T13:45:12Z"), event.timestamp());
        assertEquals(Level.INFO, event.level());
        assertEquals("CheckoutService", event.logger());
        assertEquals("Payment   accepted", event.message());
    }

    @Test
    void preservesSpacesInsideMessageAfterCorrelationId() {
        LogEvent event = parser.parse("2026-10-10T13:45:12Z INFO [CheckoutService] (req-1) charged 12.50 USD");

        assertEquals(Optional.of("req-1"), event.correlationId());
        assertEquals("charged 12.50 USD", event.message());
    }

    @Test
    void rejectsNullLine() {
        assertThrows(LogParseException.class, () -> parser.parse(null));
    }

    @Test
    void rejectsBlankLine() {
        assertThrows(LogParseException.class, () -> parser.parse("   "));
    }

    @Test
    void rejectsInvalidTimestamp() {
        assertThrows(LogParseException.class,
                () -> parser.parse("not-a-timestamp INFO [CheckoutService] hi"));
    }

    @Test
    void rejectsTimestampWithoutOffset() {
        assertThrows(LogParseException.class,
                () -> parser.parse("2026-10-10T13:45:12 INFO [CheckoutService] hi"));
    }

    @Test
    void rejectsUnknownLevel() {
        assertThrows(LogParseException.class,
                () -> parser.parse("2026-10-10T13:45:12Z LOUD [CheckoutService] hi"));
    }

    @Test
    void rejectsMissingLogger() {
        assertThrows(LogParseException.class,
                () -> parser.parse("2026-10-10T13:45:12Z INFO hi"));
    }

    @Test
    void rejectsUnclosedLoggerBracket() {
        assertThrows(LogParseException.class,
                () -> parser.parse("2026-10-10T13:45:12Z INFO [CheckoutService hi"));
    }

    @Test
    void rejectsEmptyLogger() {
        assertThrows(LogParseException.class,
                () -> parser.parse("2026-10-10T13:45:12Z INFO [] hi"));
    }

    @Test
    void rejectsMissingMessage() {
        assertThrows(LogParseException.class,
                () -> parser.parse("2026-10-10T13:45:12Z INFO [CheckoutService]"));
    }

    @Test
    void rejectsUnclosedCorrelationId() {
        assertThrows(LogParseException.class,
                () -> parser.parse("2026-10-10T13:45:12Z INFO [CheckoutService] (req-42 hi"));
    }

    @Test
    void reportsOffendingLineAndReason() {
        LogParseException exception = assertThrows(LogParseException.class,
                () -> parser.parse("bad line"));

        assertEquals("bad line", exception.line());
        assertFalse(exception.reason().isBlank());
    }

    @Test
    void parseAllSkipsBlankLines() {
        List<LogEvent> events = parser.parseAll(List.of(
                "2026-10-10T13:45:12Z INFO [A] first",
                "",
                "   ",
                "2026-10-10T13:45:13Z ERROR [B] second"));

        assertEquals(2, events.size());
        assertEquals("first", events.get(0).message());
        assertEquals("second", events.get(1).message());
        assertEquals(Level.ERROR, events.get(1).level());
    }

    @Test
    void parseAllStopsAtFirstMalformedLine() {
        LogParseException exception = assertThrows(LogParseException.class,
                () -> parser.parseAll(List.of(
                        "2026-10-10T13:45:12Z INFO [A] ok",
                        "garbage line",
                        "2026-10-10T13:45:13Z INFO [B] still ok")));

        assertEquals("garbage line", exception.line());
    }

    @Test
    void parseAllReturnsEmptyForNoLines() {
        assertEquals(List.of(), parser.parseAll(List.of()));
    }
}
