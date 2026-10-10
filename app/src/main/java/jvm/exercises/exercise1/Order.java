package jvm.exercises.exercise1;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public record Order(String customer, List<OrderLine> lines) {
    public Order {
        Objects.requireNonNull(customer, "customer");
        lines = List.copyOf(lines);
    }

    public BigDecimal revenue() {
        return lines.stream().map(OrderLine::revenue).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
