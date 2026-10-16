package jvm.exercises.exercise5;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ExpenseSummaryFormatter {
    public String format(ExpenseSummary summary) {
        return Stream.of(totalByCategory(summary), totalByMonth(summary), grandTotal(summary))
                .flatMap(List::stream)
                .collect(Collectors.joining("\n"));
    }

    private List<String> grandTotal(ExpenseSummary summary) {
        var total = summary.grandTotal().toPlainString();
        return List.of("Total: " + total);
    }

    private List<String> totalByCategory(ExpenseSummary summary) {
        return formatTotals("Spending by category:", summary.totalByCategory());
    }

    private List<String> totalByMonth(ExpenseSummary summary) {
        return formatTotals("Spending by month:", summary.totalByMonth());
    }

    private static <K extends Comparable<? super K>> List<String> formatTotals(
            String heading, Map<K, BigDecimal> totals) {
        List<String> lines = new ArrayList<>();
        lines.add(heading);

        var entries =
                totals.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList();

        for (var entry : entries) {
            lines.add("  " + entry.getKey() + ": " + entry.getValue().toPlainString());
        }

        lines.add("");
        return List.copyOf(lines);
    }
}
