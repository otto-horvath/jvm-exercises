package jvm.exercises.exercise5;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ExpenseCsvParserTest {

    @Test
    void parsesValidLine() {
        ExpenseCsvParser parser = new ExpenseCsvParser();

        Expense expense = parser.parseLine("2024-01-15,Groceries,42.50", 1);

        assertEquals(new Expense(LocalDate.of(2024, 1, 15), "Groceries", new BigDecimal("42.50")), expense);
    }

    @Test
    void trimsWhitespaceAroundFields() {
        ExpenseCsvParser parser = new ExpenseCsvParser();

        Expense expense = parser.parseLine(" 2024-01-15 , Groceries , 42.50 ", 1);

        assertEquals(new Expense(LocalDate.of(2024, 1, 15), "Groceries", new BigDecimal("42.50")), expense);
    }

    @Test
    void rejectsWrongFieldCount() {
        ExpenseCsvParser parser = new ExpenseCsvParser();

        assertThrows(ExpenseParseException.class, () -> parser.parseLine("2024-01-15,Groceries", 1));
        assertThrows(ExpenseParseException.class, () -> parser.parseLine("2024-01-15,Groceries,10.00,extra", 1));
    }

    @Test
    void rejectsBadDate() {
        ExpenseCsvParser parser = new ExpenseCsvParser();

        ExpenseParseException exception =
                assertThrows(ExpenseParseException.class, () -> parser.parseLine("not-a-date,Groceries,10.00", 4));

        assertEquals(4, exception.lineNumber());
        assertEquals("not-a-date,Groceries,10.00", exception.line());
    }

    @Test
    void rejectsBlankCategory() {
        ExpenseCsvParser parser = new ExpenseCsvParser();

        assertThrows(ExpenseParseException.class, () -> parser.parseLine("2024-01-15,  ,10.00", 1));
    }

    @Test
    void rejectsNonNumericAmount() {
        ExpenseCsvParser parser = new ExpenseCsvParser();

        assertThrows(ExpenseParseException.class, () -> parser.parseLine("2024-01-15,Groceries,abc", 1));
    }

    @Test
    void rejectsNonPositiveAmount() {
        ExpenseCsvParser parser = new ExpenseCsvParser();

        assertThrows(ExpenseParseException.class, () -> parser.parseLine("2024-01-15,Groceries,0", 1));
        assertThrows(ExpenseParseException.class, () -> parser.parseLine("2024-01-15,Groceries,-3.00", 1));
    }

    @Test
    void rejectsNullLine() {
        ExpenseCsvParser parser = new ExpenseCsvParser();

        assertThrows(NullPointerException.class, () -> parser.parseLine(null, 1));
    }

    @Test
    void parsesFileAndSkipsBlankLines(@TempDir Path tempDir) throws Exception {
        ExpenseCsvParser parser = new ExpenseCsvParser();
        Path file = tempDir.resolve("expenses.csv");
        Files.writeString(file, """
                2024-01-15,Groceries,42.50

                2024-02-01,Transport,10.00
                2024-01-20,Groceries,8.00
                """);

        List<Expense> expenses = parser.parse(file);

        assertEquals(
                List.of(
                        new Expense(LocalDate.of(2024, 1, 15), "Groceries", new BigDecimal("42.50")),
                        new Expense(LocalDate.of(2024, 2, 1), "Transport", new BigDecimal("10.00")),
                        new Expense(LocalDate.of(2024, 1, 20), "Groceries", new BigDecimal("8.00"))),
                expenses);
    }

    @Test
    void rejectsMissingFile(@TempDir Path tempDir) {
        ExpenseCsvParser parser = new ExpenseCsvParser();

        assertThrows(UncheckedIOException.class, () -> parser.parse(tempDir.resolve("missing.csv")));
    }

    @Test
    void rejectsNullPath() {
        ExpenseCsvParser parser = new ExpenseCsvParser();

        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
}
