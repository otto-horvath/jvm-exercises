package jvm.exercises.exercise5;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public record ExpenseSummary(
        Map<String, BigDecimal> totalByCategory, Map<YearMonth, BigDecimal> totalByMonth, BigDecimal grandTotal) {
    public ExpenseSummary {
        totalByCategory = normalizeTotals(totalByCategory, "totalByCategory");
        totalByMonth = normalizeTotals(totalByMonth, "totalByMonth");
        grandTotal = Objects.requireNonNull(grandTotal, "grandTotal").setScale(2, RoundingMode.HALF_UP);
    }

    private static <K> Map<K, BigDecimal> normalizeTotals(Map<K, BigDecimal> totals, String name) {
        Objects.requireNonNull(totals, name);
        return totals.entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(
                        Map.Entry::getKey, entry -> entry.getValue().setScale(2, RoundingMode.HALF_UP)));
    }
}
