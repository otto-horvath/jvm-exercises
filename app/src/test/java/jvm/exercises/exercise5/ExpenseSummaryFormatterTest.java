package jvm.exercises.exercise5;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ExpenseSummaryFormatterTest {

    private final ExpenseSummaryFormatter formatter = new ExpenseSummaryFormatter();

    @Test
    void formatsCategoryAndMonthSummaries() {
        ExpenseSummary summary = new ExpenseSummary(
                Map.of("Groceries", new BigDecimal("50.50"), "Transport", new BigDecimal("10.00")),
                Map.of(YearMonth.of(2024, 1), new BigDecimal("42.50"), YearMonth.of(2024, 2), new BigDecimal("10.00")),
                new BigDecimal("60.50"));

        String actual = formatter.format(summary);

        assertEquals(
                String.join(
                        "\n",
                        "Spending by category:",
                        "  Groceries: 50.50",
                        "  Transport: 10.00",
                        "",
                        "Spending by month:",
                        "  2024-01: 42.50",
                        "  2024-02: 10.00",
                        "",
                        "Total: 60.50"),
                actual);
    }

    @Test
    void formatsEmptySummary() {
        ExpenseSummary summary = new ExpenseSummary(Map.of(), Map.of(), BigDecimal.ZERO);

        String actual = formatter.format(summary);

        assertEquals(String.join("\n", "Spending by category:", "", "Spending by month:", "", "Total: 0.00"), actual);
    }

    @Test
    void rejectsNullSummary() {
        assertThrows(NullPointerException.class, () -> formatter.format(null));
    }
}
