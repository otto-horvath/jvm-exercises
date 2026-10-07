package jvm.exercises;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import org.jspecify.annotations.NonNull;

public record Order(String customer, List<@NonNull OrderLine> lines) {
    public Order {
        Objects.requireNonNull(customer, "customer");
        lines = List.copyOf(lines);
    }

    public BigDecimal revenue() {
        return lines.stream()
                .map(OrderLine::revenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
