# Expense-file summary CLI: a practical guide

Exercise 5 reads expenses from a file, validates each CSV row, aggregates money
by category and month, and formats a report. It brings together records,
exceptions, collections, streams, and `BigDecimal`, while adding Java's `Path`
and `Files` APIs for file input.

## Read files with `Path` and `Files`

`Path` represents a file-system location. `Files` provides operations on that
path, including reading its contents:

```java
List<String> lines = Files.readAllLines(file);
```

`readAllLines` is convenient for a small exercise file: it returns the lines in
order, making it easy to keep the parser focused on one line at a time. It also
loads the whole file into memory, so a large-file application would usually
process a buffered stream instead.

The file-reading method translates checked `IOException` into
`UncheckedIOException`, allowing callers to use the parser without adding a
checked exception to every call site. The I/O boundary stays in
`ExpenseCsvParser`; aggregation works only with `List<Expense>`.

## Keep original line numbers while skipping blanks

The parser uses an indexed loop when processing file lines:

```java
for (int i = 0; i < lines.size(); i++) {
    String line = lines.get(i);
    if (!line.isBlank()) {
        expenses.add(parseLine(line, i + 1));
    }
}
```

The index preserves the original 1-based file line number even when blank
lines are skipped. A stream `map` receives an element, not its index. An
`IntStream.range` can stream over indices, but a loop is direct when both the
element and its position matter.

The parser returns `List.copyOf(expenses)`, so callers receive an immutable
result rather than a reference to the mutable list used during construction.

## Validate a row where its error context is available

`parseLine` has both the raw input and its file line number. It checks the
number of fields and the category, parses the date and amount, and constructs
the `Expense`. Parsing failures become `ExpenseParseException` instances with
the source line and its number:

```java
try {
    date = LocalDate.parse(fields.get(0));
} catch (DateTimeParseException e) {
    throw new ExpenseParseException(lineNumber, line, "invalid date");
}
```

Keeping these validations and catches in `parseLine` avoids passing the raw
line and line number through several private field-parsing helpers. The helper
extraction was considered, but the row context made the inline version easier
to follow at this size.

`split(",", -1)` preserves a trailing empty field, so malformed rows such as
one with a missing amount still have their true field shape available for
validation. Trimming each field handles surrounding whitespace. This exercise
uses a simple comma-separated format; quoted CSV values containing commas are
outside its scope.

## Aggregate `BigDecimal` values with collectors

The aggregator groups expenses by category and `YearMonth`. A shared helper
captures the repeated `groupingBy` pattern, while a reusable collector maps each
expense to its amount, adds the amounts, and rounds the final group total:

```java
private static final Collector<Expense, ?, BigDecimal> SUM_AMOUNTS =
        Collectors.mapping(
                Expense::amount,
                Collectors.collectingAndThen(
                        Collectors.reducing(BigDecimal.ZERO, BigDecimal::add),
                        total -> total.setScale(2, RoundingMode.HALF_UP)));

private static <K> Map<K, BigDecimal> sumBy(
        List<Expense> expenses, Function<Expense, K> keyExtractor) {
    return expenses.stream().collect(Collectors.groupingBy(keyExtractor, SUM_AMOUNTS));
}
```

`sumBy` accepts a key extractor, such as `Expense::category` or a function that
converts the date to `YearMonth`. The collector is a reusable description of
the reduction; each collection operation obtains its own accumulation state.
Rounding happens after addition, not after every individual addition, so the
rounding policy is explicit and consistent.

There is no built-in `BigDecimal` equivalent of `Collectors.summingInt`; the
`mapping` plus `reducing` combination supplies the sum. The grand total uses
the same `SUM_AMOUNTS` collector without grouping.

## Make money scale part of the summary invariant

`BigDecimal` represents both a numeric value and a scale. For example,
`BigDecimal.ZERO.toPlainString()` is `"0"`, while an amount normalized to scale
2 is `"0.00"`. The summary record normalizes category totals, month totals, and
the grand total to two decimal places with `RoundingMode.HALF_UP` in its compact
constructor:

```java
grandTotal = Objects.requireNonNull(grandTotal, "grandTotal")
        .setScale(2, RoundingMode.HALF_UP);
```

The maps are also copied into unmodifiable maps while their values are
normalized. This keeps the money policy true whether a summary comes from the
aggregator or is constructed directly in a test. The formatter can then use
`toPlainString()` without choosing a rounding policy of its own.

## Format ordered map entries into report lines

Maps do not promise the desired report ordering, so the formatter sorts their
entries by key before rendering them. `entrySet()` provides each key-value
pair, and `Map.Entry.comparingByKey()` makes alphabetical category order and
chronological `YearMonth` order explicit.

Category and month sections share a `formatTotals` helper. It adds a heading,
loops over sorted entries to append formatted lines, and adds an empty string
for the blank separator. The loop keeps the list-building logic readable and
avoids producing a nested list of section lines.

The complete report is a list of sections (`List<String>` values). `flatMap`
flattens those section streams into one stream of lines, and
`Collectors.joining("\n")` creates the final report text:

```java
Stream.of(categoryLines, monthLines, grandTotalLines)
        .flatMap(List::stream)
        .collect(Collectors.joining("\n"));
```

Using `""` as a blank line and letting `joining` supply separators avoids
accidentally adding leading or trailing newlines.

## Test the layers independently, then together

The parser tests cover valid rows, whitespace, malformed fields, invalid dates
and amounts, null input, missing files, and blank lines in a file. Aggregator
tests cover empty input, grouping, totals, rounding, and null input. Formatter
tests assert the exact report text, including ordering and blank lines. The
end-to-end test writes a temporary file and exercises parsing, aggregation, and
formatting together.

Run the focused tests while working on one layer:

```shell
./gradlew test --tests jvm.exercises.exercise5.ExpenseCsvParserTest
./gradlew test --tests jvm.exercises.exercise5.ExpenseAggregatorTest
./gradlew test --tests jvm.exercises.exercise5.ExpenseSummaryFormatterTest
```

The complete test suite passed after the exercise implementation. The README's
Java 25 compact-source-file / instance-`main` experiment remains optional; the
tested exercise covers the parsing-to-report pipeline rather than a separate
command-line launcher.

## Further reading

- [`Path`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/nio/file/Path.html)
- [`Files`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/nio/file/Files.html)
- [`BigDecimal`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html)
- [`Collectors`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/stream/Collectors.html)
- [`Map.Entry`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/Map.Entry.html)
