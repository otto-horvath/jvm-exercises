# Regex-based log parsing: a practical guide

Exercise 3 parses a log line into a timestamp, level, logger, optional
correlation ID, and message. The parser uses a readable regular expression for
the line structure, then converts the timestamp and level into their Java
types. This guide covers the design choices and edge cases from the exercise.

## Build a pattern from named parts

A long regular expression can be difficult to maintain as one string. Keep
each part in a named constant, then compose the fragments:

```java
private static final String WHITESPACE = "\\s+";
private static final String TIMESTAMP = "(?<timestamp>\\S+)";
private static final String LEVEL = "(?<level>\\S+)";
private static final String LOGGER = "\\[(?<logger>[^\\]]+)]";
private static final String MESSAGE = "(?<message>\\S(?:.*\\S)?)";
private static final Pattern LOG_LINE_PATTERN = Pattern.compile(
        String.join("", List.of(
                "^\\s*",
                TIMESTAMP,
                WHITESPACE,
                LEVEL,
                WHITESPACE,
                LOGGER,
                WHITESPACE,
                MESSAGE,
                "\\s*$")));
```

`String.join("", fragments)` concatenates regex fragments without inserting
characters. Separators are explicit fragments: `\\s+` matches one or more
whitespace characters, while `\\s*` at the start and end allows optional
surrounding whitespace. Using `String.join(" ", ...)` would insert literal
spaces between every fragment instead.

Named groups such as `(?<timestamp>...)` make the result self-documenting:
`matcher.group("timestamp")` communicates more than a positional group number.
The `^` and `$` anchors require the pattern to describe the entire line rather
than just a matching substring.

## Optional fields should include their separator

The correlation ID and the whitespace before it are both optional:

```java
private static final String OPTIONAL_CORRELATION_ID =
        "(?:" + WHITESPACE + "\\((?<correlationId>[^)]+)\\))?";
```

The outer `(?:...)` groups the optional segment without creating an extra
capturing group. Its inner named group captures only the ID text, not the
parentheses. When the segment is absent, `matcher.group("correlationId")` is
`null`, which maps naturally to `Optional.empty()`:

```java
Optional.ofNullable(matcher.group("correlationId"))
```

If the whitespace before the ID were outside the optional group, it would
remain required even when there is no ID.

There is a subtle malformed-input case: an unclosed value such as `(req-42`
may be accepted as the start of the message, since a message is otherwise free
text. After a successful match, the parser checks whether the correlation ID
was absent while the message starts with `(` and rejects that line as a
malformed correlation ID.

## Match structure, then validate values

Regex is useful for recognizing the shape of a line, but the captured
timestamp and level still need semantic conversion:

```java
Instant timestamp = Instant.parse(matcher.group("timestamp"));
Level level = Level.valueOf(matcher.group("level").toUpperCase(Locale.ROOT));
```

`Instant.parse` verifies the timestamp's syntax and requires an offset in the
accepted instant format. `Level.valueOf` verifies the level is one of the
declared enum values. The parser catches those conversion failures and throws
`LogParseException` with the original input line, so callers receive a
consistent parsing error rather than low-level conversion exceptions.

For case-insensitive enum input, use `Locale.ROOT` when normalizing. The
default locale can have language-specific casing rules and is not appropriate
for machine-readable identifiers.

The message group `\\S(?:.*\\S)?` requires at least one non-whitespace
character, preserves whitespace within the message, and avoids capturing
padding at the end of the line. This lets the parser return the captured
message directly without trimming it afterward.

## Develop one behavior at a time

The exercise works well as a test-first sequence:

1. Match the basic timestamp, level, logger, and message.
2. Add the optional correlation ID and test both its presence and absence.
3. Permit whitespace around the line and between fields without losing spaces
   in the message.
4. Test invalid timestamps, unknown levels, missing fields, malformed
   brackets, and an unclosed correlation ID.
5. Add `parseAll` behavior and verify that blank lines are skipped while
   malformed lines still report an error.

Run the focused parser tests while working:

```shell
./gradlew test --tests jvm.exercises.exercise3.LogLineParserTest
```

## How this relates to Exercise 3

- `LogLineParser` keeps its regex fragments private because the pattern is an
  implementation detail used only by this parser.
- `Matcher.matches()` checks the whole line; named groups supply the values
  used to construct `LogEvent`.
- `Instant` and `Level` perform type-specific validation after structural
  matching.
- `LogParseException` gives malformed lines one consistent failure type and
  retains the offending input.
- `Optional<String>` models the genuinely absent correlation ID.

## Further reading

- [`java.util.regex.Pattern`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/regex/Pattern.html)
- [`java.util.regex.Matcher`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/regex/Matcher.html)
- [`java.time.Instant`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/time/Instant.html)
- [`java.util.Optional`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/Optional.html)
