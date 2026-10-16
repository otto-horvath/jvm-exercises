package jvm.exercises.exercise5;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ExpenseEndToEndTest {

    @Test
    void readsFileAndPrintsSummary(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("expenses.csv");
        Files.writeString(file, """
                2024-01-15,Groceries,42.50
                2024-01-20,Groceries,8.00
                2024-02-01,Transport,10.00
                """);

        ExpenseCsvParser parser = new ExpenseCsvParser();
        ExpenseAggregator aggregator = new ExpenseAggregator();
        ExpenseSummaryFormatter formatter = new ExpenseSummaryFormatter();

        String report = formatter.format(aggregator.summarize(parser.parse(file)));

        assertEquals(
                String.join(
                        "\n",
                        "Spending by category:",
                        "  Groceries: 50.50",
                        "  Transport: 10.00",
                        "",
                        "Spending by month:",
                        "  2024-01: 50.50",
                        "  2024-02: 10.00",
                        "",
                        "Total: 60.50"),
                report);
    }
}
