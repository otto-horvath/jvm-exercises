package jvm.exercises.exercise3;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class LogLineParser {
    private static final String WHITESPACE = "\\s+";
    private static final String LINE_START = "^\\s*";
    private static final String LINE_END = "\\s*$";
    private static final String TIMESTAMP = "(?<timestamp>\\S+)";
    private static final String LEVEL = "(?<level>\\S+)";
    private static final String LOGGER = "\\[(?<logger>[^\\]]+)]";
    private static final String MESSAGE = "(?<message>\\S(?:.*\\S)?)";
    private static final String OPTIONAL_CORRELATION_ID =
            "(?:" + WHITESPACE + "\\((?<correlationId>[^)]+)\\))?";

    private static final Pattern LOG_LINE_PATTERN = Pattern.compile(String.join("", List.of(
            LINE_START,
            TIMESTAMP,
            WHITESPACE,
            LEVEL,
            WHITESPACE,
            LOGGER,
            OPTIONAL_CORRELATION_ID,
            WHITESPACE,
            MESSAGE,
            LINE_END)));

    public LogEvent parse(String line) {
        if (line == null || line.isBlank()) {
            throw new LogParseException(line, "Log line is null or blank");
        }

        Matcher matcher = LOG_LINE_PATTERN.matcher(line);
        if (!matcher.matches()) {
            throw new LogParseException(line, "Log line does not match the expected format");
        }

        if (matcher.group("correlationId") == null && matcher.group("message").startsWith("(")) {
            throw new LogParseException(line, "Unclosed correlation ID");
        }

        return new LogEvent(
                parseInstant(matcher.group("timestamp"), line),
                parseLevel(matcher.group("level"), line),
                matcher.group("logger"),
                Optional.ofNullable(matcher.group("correlationId")),
                matcher.group("message"));
    }

    private Instant parseInstant(String value, String line) {
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException exception) {
            throw new LogParseException(line, "Invalid timestamp");
        }
    }

    private Level parseLevel(String value, String line) {
        try {
            return Level.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new LogParseException(line, "Unknown log level");
        }
    }

    public List<LogEvent> parseAll(List<String> lines) {
        return lines.stream()
                .filter(line -> !line.isBlank())
                .map(this::parse)
                .toList();
    }
}
