package jvm.exercises.exercise5;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ExpenseAggregatorTest {

    private final ExpenseAggregator aggregator = new ExpenseAggregator();

    @Test
    void summarizesEmptyList() {
        ExpenseSummary summary = aggregator.summarize(List.of());

        assertEquals(Map.of(), summary.totalByCategory());
        assertEquals(Map.of(), summary.totalByMonth());
        assertEquals(new BigDecimal("0.00"), summary.grandTotal());
    }

    @Test
    void summarizesSingleExpense() {
        Expense expense = new Expense(LocalDate.of(2024, 1, 15), "Groceries", new BigDecimal("42.50"));

        ExpenseSummary summary = aggregator.summarize(List.of(expense));

        assertEquals(Map.of("Groceries", new BigDecimal("42.50")), summary.totalByCategory());
        assertEquals(Map.of(YearMonth.of(2024, 1), new BigDecimal("42.50")), summary.totalByMonth());
        assertEquals(new BigDecimal("42.50"), summary.grandTotal());
    }

    @Test
    void groupsByCategoryAndMonth() {
        List<Expense> expenses = List.of(
                new Expense(LocalDate.of(2024, 1, 15), "Groceries", new BigDecimal("42.50")),
                new Expense(LocalDate.of(2024, 1, 20), "Groceries", new BigDecimal("7.50")),
                new Expense(LocalDate.of(2024, 2, 1), "Transport", new BigDecimal("10.00")));

        ExpenseSummary summary = aggregator.summarize(expenses);

        assertEquals(
                Map.of("Groceries", new BigDecimal("50.00"), "Transport", new BigDecimal("10.00")),
                summary.totalByCategory());
        assertEquals(
                Map.of(YearMonth.of(2024, 1), new BigDecimal("50.00"), YearMonth.of(2024, 2), new BigDecimal("10.00")),
                summary.totalByMonth());
        assertEquals(new BigDecimal("60.00"), summary.grandTotal());
    }

    @Test
    void roundsMoneyToTwoDecimals() {
        List<Expense> expenses = List.of(
                new Expense(LocalDate.of(2024, 1, 1), "Pennies", new BigDecimal("0.005")),
                new Expense(LocalDate.of(2024, 1, 2), "Pennies", new BigDecimal("0.004")));

        ExpenseSummary summary = aggregator.summarize(expenses);

        assertEquals(Map.of("Pennies", new BigDecimal("0.01")), summary.totalByCategory());
        assertEquals(new BigDecimal("0.01"), summary.grandTotal());
    }

    @Test
    void rejectsNullList() {
        assertThrows(NullPointerException.class, () -> aggregator.summarize(null));
    }
}
