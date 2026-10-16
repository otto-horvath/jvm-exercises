package jvm.exercises.exercise5;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public final class ExpenseAggregator {
    private static final BigDecimal ZERO_MONEY = new BigDecimal("0.00");

    private static final Collector<Expense, ?, BigDecimal> SUM_AMOUNTS = Collectors.mapping(
            Expense::amount,
            Collectors.collectingAndThen(
                    Collectors.reducing(BigDecimal.ZERO, BigDecimal::add),
                    total -> total.setScale(2, RoundingMode.HALF_UP)));

    public ExpenseSummary summarize(List<Expense> expenses) {
        Objects.requireNonNull(expenses, "expenses");
        if (expenses.isEmpty()) {
            return new ExpenseSummary(Map.of(), Map.of(), ZERO_MONEY);
        }

        var totalByCategory = sumBy(expenses, Expense::category);

        var totalByMonth = sumBy(expenses, expense -> YearMonth.from(expense.date()));

        var grandTotal = expenses.stream().collect(SUM_AMOUNTS);

        return new ExpenseSummary(totalByCategory, totalByMonth, grandTotal);
    }

    private static <K> Map<K, BigDecimal> sumBy(List<Expense> expenses, Function<Expense, K> keyExtractor) {
        return expenses.stream().collect(Collectors.groupingBy(keyExtractor, SUM_AMOUNTS));
    }
}
