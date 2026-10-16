package jvm.exercises.exercise5;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Map;
import java.util.Objects;

public record ExpenseSummary(
        Map<String, BigDecimal> totalByCategory, Map<YearMonth, BigDecimal> totalByMonth, BigDecimal grandTotal) {
    public ExpenseSummary {
        totalByCategory = Map.copyOf(Objects.requireNonNull(totalByCategory, "totalByCategory"));
        totalByMonth = Map.copyOf(Objects.requireNonNull(totalByMonth, "totalByMonth"));
        grandTotal = Objects.requireNonNull(grandTotal, "grandTotal");
    }
}
